<template>
  <div id="wrapper">
    <div id="container">
      <div id="wordCloud"></div>
      <el-divider id="divider" direction="vertical"></el-divider>
      <div id="weightedWordCloud"></div>
    </div>
    <p id="graphTitle">{{2023-year}}年度词云（原始|加权）</p>
  </div>
</template>

<script>
import * as echarts from "echarts";
import "echarts-wordcloud";
export default {
  name: 'WordCloud',
  props: [
    "year"
  ],
  data() {
    return {
      chart: undefined,
      weightedChart: undefined,
      basicOption: {
        series: [
          {
            type: "wordCloud",

            //要绘制的“云”的形状。任意极坐标方程都可以表示为a吗
            //回调函数，或关键字存在。可用的礼物为圆形(默认)，
            //心形(苹果或心形曲线，最著名的极坐标方程)，菱形(
            // alias of square)， triangle-forward, triangle， (alias of triangle- standing, pentagon, and star)
            shape: "star, triangle",

            //保持maskImage或1:1的形状的纵横比
            // echarts-wordcloud@2.1.0支持该选项
            keepAspect: false,

            //一个轮廓图像，白色区域将被排除在绘制文本之外。
            //当云的形状增长时，形状选项将继续应用。
            // maskImage: maskImage,

            //跟随左/顶/宽/高/右/底是用来定位字云
            //默认放置在中心，大小为75% x 80%。
            left: "center",
            top: "center",
            width: "95%",
            height: "95%",
            right: null,
            bottom: null,

            //文本大小范围，数据中的值将被映射到。
            //默认最小12px，最大60px大小。
            sizeRange: [25, 100],

            //文本旋转范围和步进度。文本将被rotationStep 45在[- 90,90]范围内随机旋转
            rotationRange: [-45, 90],
            rotationStep: 45,

            //网格的大小(以像素为单位)，用于标记画布的可用性
            //网格大小越大，单词之间的间距越大。
            gridSize: 8,

            //设置为true允许文字部分绘制在画布外。
            //允许文字大于画布的大小
            drawOutOfBound: false,

            //如果执行布局动画。
            //注意当有很多字的时候禁用它会导致UI阻塞。
            layoutAnimation: true,

            // Global text style
            textStyle: {
              fontFamily: "smiley-sans",
              fontWeight: "bold",
              // Color can be a callback function or a color string
              color: function () {
                // Random color
                return (
                  "rgb(" +
                  [
                    Math.round(Math.random() * 160),
                    Math.round(Math.random() * 160),
                    Math.round(Math.random() * 160),
                  ].join(",") +
                  ")"
                );
              },
            },
            emphasis: {
              focus: "self",
            }
          }
        ]
      }
    }
  },
  watch: {
    year(newYear) {
      this.update(newYear);
    }
  },
  mounted() {
    this.chart = this.initChart("wordCloud");
    this.weightedChart = this.initChart("weightedWordCloud");
    this.update(this.year);
  },
  methods: {
    initChart(elementId) {
      let accessToElements = document.getElementById(elementId);
      let themeStyle = echarts.init(accessToElements);
      // 图表自适应配置
      let chartNode = new ResizeObserver(() => {
        themeStyle.resize();
      });
      chartNode.observe(accessToElements);
      return themeStyle;
    },
    getData(path) {
      let xhr = new XMLHttpRequest();
      xhr.open("GET", path, false); // public文件夹下的绝对路径
      xhr.overrideMimeType("text/html;charset=utf-8");
      xhr.send(null);
      let data = [];
      let rawText = xhr.responseText;
      for (let tuple of rawText.substring(2, rawText.length - 2).split("), (")) {
        data.push({
          "name": tuple.split(',')[1],
          "value": tuple.split(',')[0]
        })
      }
      return data;
    },
    getWordCloud(chart, data) {
      let option = this.basicOption;
      option.series[0].data = data;
      // 绘制图表
      option && chart.setOption(option);
    },
    update(year) {
      this.getWordCloud(this.chart, this.getData("wordCloud/" + year + ".txt"));
      this.getWordCloud(this.weightedChart, this.getData("wordCloud/w" + year + ".txt"));
    }
  }
}
</script>

<style scoped>
#wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

#container {
  width: 100%;
  height: 450px;
  margin: 1em auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#wordCloud {
  width: 48%;
  height: 100%;
}

#divider {
  width: 2%;
  height: 80%;
}

#weightedWordCloud {
  width: 48%;
  height: 100%;
}

#graphTitle {
  font-family: "smiley-sans";
  font-size: 2em;
}

</style>
