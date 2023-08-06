<!-- 个人中心页面，展示出个人信息，并可点击修改 -->
<template>
    <template v-if="user">
        <van-cell title="昵称" is-link :value="user.username" @click="toEdit('username', '昵称', user.username)" />
        <van-cell title="账号" is-link to="/user/edit" :value="user.userAccount" />
        <van-cell title="头像" is-link to="/user/edit">
            <img style="height:48px" :src="user.avatarUrl">
        </van-cell>
        <van-cell title="性别" is-link to="/user/edit" :value="user.gender" />
        <van-cell title="电话" is-link to="/user/edit" :value="user.phone" />
        <van-cell title="邮箱" is-link to="/user/edit" :value="user.email" />
        <van-cell title="星球编号" :value="user.planetCode" />
        <van-cell title="注册时间" :value="user.createTime.toISOString()" />
    </template>
</template>

<script setup>
import { useRouter } from 'vue-router';
import myAxios from '../plugins/myAxios';
import { onMounted, ref } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';

// const user = {
//     username: '卡布其诺',
//     userAccount: 'Milk',
//     avatarUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/logo.png',
//     gender: '女',
//     phone: '12345678',
//     email: '122557743@qq.com',
//     planetCode: '1234',
//     createTime: new Date(),
// };

// user设置为响应式
const user = ref()
// 页面一挂载就执行，从后端 user/current 获取当前用户信息
onMounted(async () => {
    const res = await myAxios.get('/user/current')
    if (res.code === 0 && res.data > 0) {
        user.value = res.data
        showSuccessToast('获取用户信息成功')
    } else {
        showFailToast('获取用户信息失败')
    }
})

const router = useRouter()
// 使用 router路由传递数据到 /user/edit 编辑页面（前端）
const toEdit = (editKey, editName, currentValue) => {
    router.push({
        // 要传递到的路径
        path: '/user/edit',
        // 要传递的参数
        query: {
            editKey,
            editName,
            currentValue,
        }
    })
}
</script>


<style scoped></style>