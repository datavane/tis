const path = require('path');

/**
 * 基础Webpack配置
 *
 * 适用于简单的单组件插件
 */
module.exports = {
  mode: 'production',

  entry: './src/index.ts',

  output: {
    filename: 'component.bundle.js',
    path: path.resolve(__dirname, '../src/main/resources/META-INF/webapp/plugin-assets'),
    clean: true,
    library: {
      type: 'umd',
      name: 'TISWebComponent'
    }
  },

  resolve: {
    extensions: ['.ts', '.js'],
    mainFields: ['browser', 'module', 'main']
  },

  module: {
    rules: [
      {
        test: /\.ts$/,
        use: 'ts-loader',
        exclude: /node_modules/
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.(png|jpg|gif|svg)$/,
        type: 'asset/inline'
      }
    ]
  },

  optimization: {
    minimize: true,
    splitChunks: false // 单文件输出
  },

  devtool: 'source-map',

  stats: {
    colors: true,
    modules: false,
    children: false
  },

  performance: {
    hints: 'warning',
    maxAssetSize: 500 * 1024, // 500KB
    maxEntrypointSize: 500 * 1024
  }
};
