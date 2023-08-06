import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite';
import { VantResolver } from 'unplugin-vue-components/resolvers';


// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    Components({
      // 按需引入配置
      resolvers: [VantResolver()],
    }),
  ],
  // server:{
  //   proxy: { 
  //     '/api': {
  //      target: "http://127.0.0.1:8082/api",
  //      changeOrigin: true,
  //      rewrite: (path) => path.replace(/^\/api/, '')
  //     }
  //   }
  // }
})

