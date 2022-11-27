<template>
  <figure class="highcharts-figure">
    <div id="parent-container">
      <div id="play-controls">
        <el-button id="play-pause-button" type="primary" :icon="btnIcon" circle @click="clickButton"/>
        <el-slider id="play-range" v-model="curMonth" :min="1" :max="59" @input="update(val - {startMonth})" :format-tooltip="getMonthName"/>
      </div>
      <div id="container"></div>
    </div>
    <p class="highcharts-description">
      本图数据共涵盖2018年1月至2022年11月下旬的热点问题标签。
    </p>
  </figure>
</template>

<script>
import Highcharts from "highcharts";
import { VideoPause, VideoPlay } from '@element-plus/icons-vue';
import Dataset from '../../public/dynamicBarChart/count.json';
import { markRaw } from "vue";

export default {
  name: 'DynamicBarChart',
  data() {
    return {
      iconPlay: markRaw(VideoPlay),
      iconPause: markRaw(VideoPause),
      btnIcon: undefined,

      startMonth: 1,
      endMonth: 59,
      curMonth: 1,
      nbr: 15,

      chart: undefined,
      dataset: Dataset
    }
  },
  mounted() {
    this.btnIcon = this.iconPlay;
    this.animate(Highcharts);
    this.chart = Highcharts.chart("container", {
      chart: {
        animation: {
          duration: 500
        },
        marginRight: 50
      },
      title: {
        text: '问题标签频数/按月累计',
        align: 'left',
      },
      subtitle: {
        useHTML: true,
        text: this.getSubtitle(),
        floating: true,
        align: 'right',
        verticalAlign: 'middle',
        y: -20,
        x: -100
      },
      legend: {
        enabled: false
      },
      xAxis: {
        type: "category"
      },
      yAxis: {
        opposite: true,
        tickPixelInterval: 150,
        title: {
          text: null
        }
      },
      plotOptions: {
        series: {
          animation: false,
          groupPadding: 0,
          pointPadding: 0.1,
          borderWidth: 0,
          colorByPoint: true,
          dataSorting: {
            enabled: true,
            matchByName: true
          },
          type: "bar",
          dataLabels: {
            enabled: true
          }
        }
      },
      series: [
        {
          type: 'bar',
          name: this.getMonthName(this.startMonth),
          data: this.getData(this.startMonth)[1]
        }
      ],
      responsive: {
        rules: [{
          condition: {
            maxWidth: 550
          },
          chartOptions: {
            xAxis: {
              visible: false
            },
            subtitle: {
              x: 0
            },
            plotOptions: {
              series: {
                dataLabels: [{
                  enabled: true,
                  y: 8
                }, {
                  enabled: true,
                  format: '{point.name}',
                  y: -8,
                  style: {
                    fontWeight: 'normal',
                    opacity: 0.7
                  }
                }]
              }
            }
          }
        }]
      }
    });
  },
  methods: {
    getMonthName(month) {
      return String(201801 + Math.floor((month - 1) / 12) * 100 + ((month - 1) % 12));
    },

    getData(month) {
      const output = Object.entries(this.dataset).map(tag => {
        const [tagName, tagData] = tag;
        return [tagName, Number(tagData[month])];
      }).sort((a, b) => b[1] - a[1]);
      return [output[0], output.slice(0, this.nbr)];
    },
    getSubtitle() {
      // const total = (this.getData(this.curMonth)[0][1]).toFixed(0);
      return `<span style="font-family: smiley-sans; font-size: 80px">${this.getMonthName(this.curMonth)}</span>`;
    },
    animate(H) {
      const FLOAT = /^-?\d+\.?\d*$/;

      // Add animated textSetter, just like fill/strokeSetters
      H.Fx.prototype.textSetter = function () {
        let startValue = this.start.replace(/ /g, ""), 
            endValue = this.end.replace(/ /g, ""),
            currentValue = this.end.replace(/ /g, "");

        if ((startValue || "").match(FLOAT)) {
          startValue = parseInt(startValue, 10);
          endValue = parseInt(endValue, 10);

          // No support for float
          currentValue = Highcharts.numberFormat(
            Math.round(startValue + (endValue - startValue) * this.pos),
            0
          );
        }

        this.elem.endText = this.end;

        this.elem.attr(this.prop, currentValue, null, true);
      };

      // Add textGetter, not supported at all at this moment:
      H.SVGElement.prototype.textGetter = function () {
        const ct = this.text.element.textContent || "";
        return this.endText ? this.endText : ct.substring(0, ct.length / 2);
      };

      // Temporary change label.attr() with label.animate():
      // In core it's simple change attr(...) => animate(...) for text prop
      H.wrap(H.Series.prototype, "drawDataLabels", function (proceed) {
        const attr = H.SVGElement.prototype.attr,
          chart = this.chart;

        if (chart.sequenceTimer) {
          this.points.forEach(point =>
            (point.dataLabels || []).forEach(
              label =>
                (label.attr = function (hash) {
                  if (hash && hash.text !== undefined) {
                    const text = hash.text;

                    delete hash.text;

                    return this
                      .attr(hash)
                      .animate({ text });
                  }
                  return attr.apply(this, arguments);

                })
            )
          );
        }

        const ret = proceed.apply(
          this,
          Array.prototype.slice.call(arguments, 1)
        );

        this.points.forEach(p =>
          (p.dataLabels || []).forEach(d => (d.attr = attr))
        );

        return ret;
      });
    },

    update(increment) {
      if (increment) {
        this.curMonth = this.curMonth + increment;
      }
      if (this.curMonth >= this.endMonth) {
        this.pause();
      }

      this.chart.update(
        {
          subtitle: {
            text: this.getSubtitle()
          }
        },
        false,
        false,
        false
      );

      this.chart.series[0].update({
        name: this.getMonthName(this.curMonth),
        data: this.getData(this.curMonth)[1]
      });
    },
    pause() {
      this.btnIcon = this.iconPlay;
      clearTimeout(this.chart.sequenceTimer);
      this.chart.sequenceTimer = undefined;
    },
    play() {
      let that = this;
      this.btnIcon = this.iconPause;
      this.chart.sequenceTimer = setInterval(function () {
        that.update(1);
      }, 500);
    },
    clickButton() {
      if (this.chart.sequenceTimer) {
        this.pause();
      } else {
        this.play();
      }
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.highcharts-figure {
  margin: 0;
}

#play-controls {
  max-width: 1000px;
  margin: 1em auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#play-pause-button {
  margin: 1em;
}

#play-range {
  margin: 1em;
}

#container {
  height: 600px;
  max-width: 1000px;
  margin: 0 auto;
}
.highcharts-description {
  max-width: 1000px;
  margin: 2em auto;
}
</style>
