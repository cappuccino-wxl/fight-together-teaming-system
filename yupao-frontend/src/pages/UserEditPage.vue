<!-- 用户中心页面点击后在这个页面修改对应的某一项 -->
<template>
  <van-form @submit="onSubmit">
    <van-field v-model="editUser.currentValue" :name="editUser.editKey" :label="editUser.editName"
      :placeholder="`请输入${editUser.editName}`" />
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<script setup>
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import myAxios from "../plugins/myAxios";
import { showFailToast, showSuccessToast } from "vant";
import { getCurrentUser } from "../services/user";

const route = useRoute();
const router = useRouter();

// 通过route接收从 UserPage 传来的数据，设为响应式以便修改完可以在页面显示新的
const editUser = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName,
})

// 点击提交按钮
const onSubmit = async () => {
  // 调用 getCurrentUser() 函数获取当前用户
  const currentUser = await getCurrentUser();
  // 如果当前用户是空的，表示用户未登录
  if (!currentUser) {
    // Toast.fail('用户未登录');
    showFailToast('请求失败')
    return;
  }
  console.log(currentUser, '当前用户')
  // 向后端发送 post 请求，把要更新的数据传给后端，传用户id和修改后的内容，更新数据库
  const res = await myAxios.post('/user/update', {
    'id': currentUser.id,
    [editUser.value.editKey]: editUser.value.currentValue,
  })
  console.log(res, '更新请求');
  if (res.code === 0 && res.data > 0) {
    // Toast.success('修改成功');
    showSuccessToast('修改成功')
    // 更新完跳转回去
    router.back();
  } else {
    // Toast.fail('修改错误');
    showFailToast('修改错误')
  }
};

</script>

<style scoped></style>
