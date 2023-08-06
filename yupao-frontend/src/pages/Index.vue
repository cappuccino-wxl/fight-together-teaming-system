<template>
  <van-cell center title="心动模式">
    <template #right-icon>
      <van-switch v-model="isMatchMode" size="24" />
    </template>
  </van-cell>
  <!-- 引用了用户卡片组件 -->
  <!-- 是否显示骨架屏，传 false 时会展示子组件内容 -->
  <user-card-list :user-list="userList" :loading="loading" />
  <van-empty v-if="!userList || userList.length < 1" description="数据为空" />
</template>
  
<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import myAxios from "../plugins/myAxios";
import { showFailToast } from "vant";
import UserCardList from "../components/UserCardList.vue";
import { UserType } from '../models/user';

const isMatchMode = ref<boolean>(false); // 默认普通模式
const userList = ref([]);
const loading = ref(true);

/**
 * 加载数据
 */
const loadData = async () => {
  let userListData: any;
  loading.value = true;
  // 心动模式，根据标签匹配用户（但是现在还没登录）
  if (isMatchMode.value) {
    const num = 10;
    // 获取从后端 /user/match 返回的匹配用户排序列表
    userListData = await myAxios.get('/user/match', {
      params: {
        num,
      },
    })
      .then(function (response) {
        console.log('/user/match succeed', response);
        return response?.data;
      })
      .catch(function (error) {
        console.error('/user/match error', error);
        showFailToast('请求失败');
      })
  } else {
    // 普通模式，直接分页查询用户
    userListData = await myAxios.get('/user/recommend', {
      params: {
        pageSize: 5,
        pageNum: 1,
      },
    })
      .then(function (response) {
        console.log('/user/recommend succeed', response);
        return response?.data?.records;
      })
      .catch(function (error) {
        console.error('/user/recommend error', error);
        showFailToast('请求失败')
      })
  }
  // 如果用户列表不为空
  if (userListData) {
    // 遍历列表
    userListData.forEach((user: UserType) => {
      if (user.tags) {
        // 把用户标签由json数组转换为字符串数组
        user.tags = JSON.parse(user.tags);
      }
    })
    userList.value = userListData;
  }
  loading.value = false;
}

// 一点击开关就加载数据
watchEffect(() => {
  loadData();
})

</script>

<style scoped></style>
  