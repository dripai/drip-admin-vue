<template>
  <a-result status="403" title="无权限" sub-title="当前账号无权限访问该页面"
    ><template #extra
      ><a-button type="primary" @click="$router.replace('/')">返回首页</a-button></template
    ></a-result
  >
</template>
