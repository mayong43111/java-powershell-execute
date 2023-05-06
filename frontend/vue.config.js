const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  configureWebpack: {
    plugins: [
      new MonacoWebpackPlugin(),
    ],
  },
})
