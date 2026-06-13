const path = require('path');

/**
 * 外部化依赖的Webpack配置
 *
 * 将Angular等公共库标记为external，减小bundle大小
 * 前提：主应用已经加载了这些库
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
      name: 'TISWebComponent',
      umdNamedDefine: true
    }
  },

  resolve: {
    extensions: ['.ts', '.js']
  },

  module: {
    rules: [
      {
        test: /\.ts$/,
        use: 'ts-loader',
        exclude: /node_modules/
      }
    ]
  },

  // 外部化依赖 - 这些库需要主应用提前加载
  externals: {
    // Angular核心库
    '@angular/core': {
      commonjs: '@angular/core',
      commonjs2: '@angular/core',
      amd: '@angular/core',
      root: ['ng', 'core']
    },
    '@angular/common': {
      commonjs: '@angular/common',
      commonjs2: '@angular/common',
      amd: '@angular/common',
      root: ['ng', 'common']
    },
    '@angular/platform-browser': {
      commonjs: '@angular/platform-browser',
      commonjs2: '@angular/platform-browser',
      amd: '@angular/platform-browser',
      root: ['ng', 'platformBrowser']
    },
    '@angular/forms': {
      commonjs: '@angular/forms',
      commonjs2: '@angular/forms',
      amd: '@angular/forms',
      root: ['ng', 'forms']
    },

    // RxJS
    'rxjs': {
      commonjs: 'rxjs',
      commonjs2: 'rxjs',
      amd: 'rxjs',
      root: 'rxjs'
    },
    'rxjs/operators': {
      commonjs: 'rxjs/operators',
      commonjs2: 'rxjs/operators',
      amd: 'rxjs/operators',
      root: ['rxjs', 'operators']
    },

    // Zone.js
    'zone.js': {
      commonjs: 'zone.js',
      commonjs2: 'zone.js',
      amd: 'zone.js',
      root: 'Zone'
    },

    // ng-zorro-antd (如果主应用已加载)
    'ng-zorro-antd': {
      commonjs: 'ng-zorro-antd',
      commonjs2: 'ng-zorro-antd',
      amd: 'ng-zorro-antd',
      root: 'ngZorro'
    }
  },

  optimization: {
    minimize: true,
    splitChunks: false
  },

  devtool: 'source-map',

  performance: {
    hints: 'warning',
    maxAssetSize: 200 * 1024, // 200KB (因为externals，会更小)
    maxEntrypointSize: 200 * 1024
  }
};
