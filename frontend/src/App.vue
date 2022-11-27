<template>
  <el-container style="margin-left: 4em; margin-right: 4em">
    <el-header>
      <el-menu
        :default-active="activeIndex"
        mode="horizontal"
        :ellipsis="false"
        @select="handleSelect"
      >
        <el-menu-item index="0">
          <img alt="logo" src="./assets/stack-overflow.png" width="50">
          <div>stack <b>overflow</b> Question Analysis</div>
        </el-menu-item>
        <div class="flex-grow" />
        <el-sub-menu index="1">
          <template #title>年度词云</template>
          <el-menu-item index="1-1">2022</el-menu-item>
          <el-menu-item index="1-2">2021</el-menu-item>
          <el-menu-item index="1-3">2020</el-menu-item>
          <el-menu-item index="1-4">2019</el-menu-item>
          <el-menu-item index="1-5">2018</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="2">热点跟踪</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <WordCloud v-if="activeIndex === '1'" :year="year"></WordCloud>
      <DynamicBarChart v-else-if="activeIndex === '2'"></DynamicBarChart>
      <div v-else id="placeholder">
        <p>欢迎！</p>
        <!-- <p>Welcome!</p> -->
      </div>
    </el-main>
  </el-container>
  
</template>

<script>
import WordCloud from './components/WordCloud.vue'
import DynamicBarChart from './components/DynamicBarChart.vue';

export default {
  name: 'App',
  components: {
    WordCloud,
    DynamicBarChart
  },
  data() {
    return {
      activeIndex: '0',
      year: '1',
    }
  },
  methods: {
    handleSelect(key) {
      this.activeIndex = key.substring(0, 1);
      if (key.length > 1) {
        this.year = key.substring(2, 3);
      }
    }
  }
}
</script>

<style>
.flex-grow {
  flex-grow: 1;
}

#placeholder {
  max-width: 1000px;
  height: 500px;
  margin: 1em auto;
  display: flex;
  justify-content: center;
  align-items: center;
}

#placeholder p {
  font-family: "smiley-sans";
  font-size: 3em;
}
</style>
