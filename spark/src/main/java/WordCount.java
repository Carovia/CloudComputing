import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;

import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class WordCount {

    public static int count=0;
    public static void main(String[] args) throws InterruptedException {

        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("StreamWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(30));

        JavaDStream<String> lines = jssc.textFileStream("hdfs://172.19.240.164:9000/input_data");
//        JavaDStream<String> lines = jssc.textFileStream("/root/input_data");

        // Split each line into words
        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(",")[x.split(",").length-1].split(";")).iterator());

        // Count each word in each batch
        JavaPairDStream<String, Integer> pairs = words.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey((i1, i2) -> i1 + i2);

        // Print the first ten elements of each RDD generated in this DStream to the console
        wordCounts.print();
//        wordCounts.saveAsHadoopFiles("hdfs://172.19.240.164:9000/output_data/","2.txt");

        wordCounts.foreachRDD(new VoidFunction<JavaPairRDD<String,Integer>>() {
            @Override
            public void call(JavaPairRDD<String,Integer> javaPairRDD) throws Exception {
                JavaRDD<String> newRdd = javaPairRDD.map(new Function<Tuple2<String, Integer>, String>() {
                    @Override
                    public String call(Tuple2<String, Integer> recod) throws Exception {
                        return recod._2+"_"+recod._1;
                    }
                });


                JavaPairRDD<Integer,String> fRdd=newRdd.mapToPair(new PairFunction<String, Integer,String>() {
                    @Override
                    public Tuple2<Integer,String> call(String s) throws Exception {
                        String[] vals=s.split("_");
                        if(vals.length<2)
                            return new Tuple2<>(0,"x");
                        return new Tuple2<Integer,String>(Integer.valueOf(vals[0]),vals[1]);
                    }
                }).sortByKey(false);
                processJavaRDDData(fRdd);
            }
        });



        jssc.start();              // Start the computation
        jssc.awaitTermination();   // Wait for the computation to terminate

//        SparkConf conf = new SparkConf().setAppName("myCount").setMaster("local");
//        JavaSparkContext sc = new JavaSparkContext(conf);
//
//        // 创建lines RDD
//
//        /*  //文本中数据
//            spark hadoop oracle
//            hive spark oracle
//            oracle spark hbase
//        * */
//        JavaRDD<String> lines = sc.textFile("/test_input/t.txt");
//
//        //将文本中数据按照空格进行切分，得到一个个单词 value
//        JavaRDD<String> wordRdd = lines.flatMap(new FlatMapFunction<String, String>() {
//
//            @Override
//            public Iterator<String> call(String s) throws Exception {
//                String[] fields = s.split(" ");
//                List<String> list = Arrays.asList(fields);
//                Iterator<String> iterator = list.iterator();
//                return iterator;
//            }
//        });
//
//        //将单词value加上标签，每个单词打成标签成(value,1)
//        JavaPairRDD<String, Integer> wordKeyValueRdd = wordRdd.mapToPair(new PairFunction<String, String, Integer>() {
//            @Override
//            public Tuple2<String, Integer> call(String s) throws Exception {
//                Tuple2<String, Integer> word = new Tuple2<>(s, 1);
//                return word;
//            }
//        });
//
//        //将达成标签的(value,1)中的数量进行相加,得出每个单词的数量
//        JavaPairRDD<String, Integer> resultRDD = wordKeyValueRdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
//            @Override
//            public Integer call(Integer integer, Integer integer2) throws Exception {
//                return integer + integer2;
//            }
//        });
//        /*    //结果输出到控制台
//        resultRDD.foreach(new VoidFunction<Tuple2<String, Integer>>() {
//            @Override
//            public void call(Tuple2<String, Integer> tuple2) throws Exception {
//                System.out.println("单词"+tuple2._1+"的数量->"+tuple2._2);
//            }
//        });
//        */
//
//        /*
//        单词spark的数量->3
//        单词hive的数量->1
//        单词hadoop的数量->1
//        单词oracle的数量->3
//        单词hbase的数量->1
//        * */
//
//        //收集RDD
//        Iterator<Tuple2<String, Integer>> iter = resultRDD.sortByKey(false).collect().iterator();
//
//        while(iter.hasNext()){
//            Tuple2<String, Integer> result = iter.next();
//            System.out.println("单词"+result._1+"的数量->"+result._2);
//        }
//
//        sc.stop();
    }

    private static void processJavaRDDData(JavaPairRDD<Integer, String> fRdd) throws Exception {
        List<Tuple2<Integer,String>> orderList=fRdd.collect();
        List<Tuple2<Integer,String>> top=null;
        if (null!=orderList&&orderList.size()>=30){
            top=orderList.subList(0,30);
        }else{
            top=orderList;
        }
        if(top!=null && top.size()!=0){
            System.out.println(top.toString());
            count++;
            System.out.println(count);
            File file = new File("/root/output_data/"+count+".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(top.toString());
            bw.close();
        }
    }
}
