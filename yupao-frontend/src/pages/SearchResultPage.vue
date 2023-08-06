<!-- 标签搜索结果页 -->
<template>
    <!-- 引用 UserListCard 组件，显示用户卡片 -->
    <user-card-list :user-list="userList" />
    <!-- 如果没有选中标签，搜索结果为空 -->
    <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空" />
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { onMounted, ref } from 'vue'
import { showFailToast } from 'vant'
// qs是npm仓库所管理的包
import qs from 'qs'
import myAxios from '../plugins/myAxios'
import UserCardList from "../components/UserCardList.vue";

const route = useRoute()
// 通过路由 route.query 把 SearchPage 的参数传递过来
const tags = route.query
// 给 userList 赋初值
const userList = ref([])

// onMounted：钩子函数，一挂载就执行
// 加了 async， await 表示在执行完整个函数后才把值赋给 userListData
onMounted(async () => {
    // 根据 tags 从后端 /user/search/tags 获取含有搜索标签的用户数组
    const userListData = await myAxios.get('/user/search/tags', {
        // tags 为从 '/search' SearchPage 传过来的参数，标签列表，为json数组
        params: {
            tagNameList: tags
        },
        // 把 tags 数组处理成URL格式
        paramsSerializer: params => {
            // qs.stringify()作用是将对象或者数组序列化成URL的格式
            // indices:false，去除默认处理的方式(带数组下标)，不带下标
            return qs.stringify(params, { indices: false })
        }
    })
        .then(function (response) {
            console.log('/user/search/tags succeed', response);
            // response 里有两层data，第一层为axios本身的相关数据，第二层为我们储存的数据
            return response?.data;
        })
        .catch(function (error) {
            console.error('/user/search/tags error', error);
            // Toast.fail('请求失败');
            showFailToast('请求失败')
        })
    console.log("userListData: ", userListData)
    if (userListData) {
        // 遍历后端传递过来的用户列表，将标签由json格式转为tags字符串数组（这步在后端处理也可以）
        userListData.forEach((user: { tags: string }) => {
            if (user.tags) {
                console.log("user.tags: ", user.tags)
                user.tags = JSON.parse(user.tags);
            }
        })
        userList.value = userListData;
    }
})

// const userList = {
//     id: 21066,
//     username: "卡布奇诺的奶茶",
//     userAccount: "123",
//     avatarUrl: "",
//     profile: "今天学习了很多内容，很开心！",
//     gender: 0,
//     phone: "12345678911",
//     email: "123@qq.com",
//     userRole: 0,
//     planetCode: '1234',
//     createTime: new Date(),
//     tags: ['java', 'happy', 'html', 'hard', 'sun', 'css'],
//   };



</script>

<style></style>