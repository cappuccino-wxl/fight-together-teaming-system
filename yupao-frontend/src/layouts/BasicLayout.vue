<!-- 公共布局 -->
<template>
    <!-- 顶部导航栏，点击左箭头返回前一页面，点击搜索键跳转到search页面 -->
    <van-nav-bar :title="title" left-arrow @click-left="onClickLeft" @click-right="onClickRight">
        <template #right>
            <van-icon name="search" size="18" />
        </template>
    </van-nav-bar>
    <!-- 这个，千万一定要加，APP.vue顶级路由，引用了这个组件，这个组件；里面再利用路由跳转跳到其他页面，
        再次创建的路由是属于二级路由 -->
    <div id="content">
        <router-view />
    </div>

    <!-- 底部导航栏，点击不同按钮跳转到不同页面 -->
    <van-tabbar @change="onChange">
        <van-tabbar-item to="/" icon="home-o" name="index">主页</van-tabbar-item>
        <van-tabbar-item to="/team" icon="search" name="team">队伍</van-tabbar-item>
        <van-tabbar-item to="/user" icon="friends-o" name="user">个人</van-tabbar-item>
    </van-tabbar>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import { useRouter, useRoute } from 'vue-router';
import routes from "../config/route";

const router = useRouter();
const route = useRoute();
const DEFAULT_TITLE = '伙伴匹配';
const title = ref(DEFAULT_TITLE);

// 根据路由切换标题
router.beforeEach((to, from) => {
    const toPath = to.path;
    const route = routes.find((route) => {
        return toPath == route.path;
    })
    title.value = route?.title ?? DEFAULT_TITLE;
})
// 点击左箭头返回前一个页面
const onClickLeft = () => {
    router.back()
};
// 点击搜索按钮跳转到SearchPage
const onClickRight = () => {
    router.push('/search')
};
// 切换底部导航栏时触发
const onChange = (index) => showToast(`标签 ${index}`);
</script>
 
<style scoped>
#content {
    padding-bottom: 50px;
}
</style>