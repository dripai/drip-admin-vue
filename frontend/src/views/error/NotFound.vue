<template>
  <a-result status="404" title="页面不存在" sub-title="请求的页面不存在或已被删除"
    ><template #extra
      ><a-button type="primary" @click="$router.replace('/')">返回首页</a-button></template
    ></a-result
  >
</template>
