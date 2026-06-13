const path = require('path');
const AngularCompilerPlugin = require('@ngtools/webpack').AngularWebpackPlugin;

module.exports = {
  entry: './src/jdbc-type-selector/index.ts',

  output: {
    filename: 'jdbc-type-selector.bundle.js',
    path: path.resolve(__dirname, '../src/main/resources/META-INF/webapp/plugin-assets'),
    clean: true,
    library: {
      type: 'umd',
      name: 'TISJdbcTypeSelector'
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
        use: '@ngtools/webpack',
        exclude: /node_modules/
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      }
    ]
  },

  plugins: [
    new AngularCompilerPlugin({
      tsConfigPath: path.resolve(__dirname, 'tsconfig.json'),
      sourceMap: true,
      jitMode: false
    })
  ],

  // 外部化共享依赖（可选，减小bundle大小）
  // 注意：主应用需要先加载这些库
  externals: {
    // '@angular/core': 'ng.core',
    // '@angular/common': 'ng.common',
    // '@angular/platform-browser': 'ng.platformBrowser',
    // '@angular/forms': 'ng.forms',
    // 'rxjs': 'rxjs',
    // 'zone.js': 'Zone'
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
  }
};
