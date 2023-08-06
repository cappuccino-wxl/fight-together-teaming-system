<!-- 搜索页，搜索标签 -->
<template>
  <!-- 搜索框 -->
<form action="/">
    <van-search
        v-model="searchText"
        show-action
        placeholder="请输入要搜索的标签"
        @search="onSearch"
        @cancel="onCancel"
    />
</form>
<!-- 搜索按钮 -->
<div style="padding: 20px">
  <van-button block type="primary" @click="doSearchResult">搜索</van-button>
</div>
<van-divider content-position="left">已选标签</van-divider>
<div v-if="activeIds.length === 0">请选择标签</div>
<!-- 展示标签 -->
<van-row gutter="16" style="padding: 0 16px;">
    <van-col  v-for="tag in activeIds">
    <van-tag closeable size="small" type="primary" @close="doClose(tag)">
        {{ tag }}
    </van-tag>
    </van-col>
</van-row>
<van-divider content-position="left">选择标签</van-divider>
<!-- 标签选择器 -->
<van-tree-select
  v-model:active-id="activeIds"
  v-model:main-active-index="activeIndex"
  :items="tagList"
/>
</template>

<script setup>
import { ref } from 'vue';
import { showToast } from 'vant';
import { useRouter } from 'vue-router'

const router = useRouter()
// searchText，activeIds，activeIndex响应式数据
const searchText = ref('');
// activeIds 绑定了所有选中的标签，是一个字符串数组
const activeIds = ref([]);
const activeIndex = ref(0);

// 标签列表
// 原始数组，不能改变，防止越搜越少
// 两级嵌套，第一层为大类，第二层才是具体的标签
const originTagList = [
    {
        text: '性别',
        children: [
          { text: '男', id: '男' },
          { text: '女', id: '女' },
        ],
    },
    {
        text: '江苏',
        children: [
          { text: '南京', id: '南京' },
          { text: '无锡', id: '无锡' },
          { text: '徐州', id: '徐州' },
        ],
    },
    { 
      text: '年级', 
        children: [
          { text: '大一', id: '大一' },
          { text: '大二', id: '大二' },
          { text: '大三', id: '大三' },
          { text: '大四', id: '大四' },
          { text: '研二', id: '研二' },
          { text: '研一', id: '研一' },
        ],
    },
];

// 复制原数组，避免修改原数组，且实现响应式
let tagList = ref(originTagList)
// 点击 取消 按钮，把搜索框内容设置为空，恢复标签数组
const onCancel = () => {
  searchText.value = ''
  tagList.value = originTagList
};

// 根据搜索词搜索标签
const onSearch = (val) => {
  tagList.value = originTagList.map(parentTag => {
    // 子数组也要赋一个新数组（标签列表）
    const tempChildren = [...parentTag.children]
    // 父对象也要赋一个新对象（标签分类）
    const tempParentTag = {...parentTag}
    // 过滤掉第二层标签
    tempParentTag.children = tempChildren.filter(item => {
      // tagList标签列表里面是否有搜索词
      item.text.includes(searchText.value)
    })
    return tempParentTag
  })
};
// 点击×取消选中标签，把这个标签过滤掉
const doClose = (tag)=>{
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag
  })
}
// 点击 搜索 按钮， 
const doSearchResult = () => {
  // 跳转到搜索结果页面，显示了多个用户卡片，包含要搜索的标签
  router.push({
    path: '/user/list',
    // 把 标签列表 传给 SearchResultPage
    query: {
      tags: activeIds.value
    }
  })
}
</script>


<style scoped>
</style>