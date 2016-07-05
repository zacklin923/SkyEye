angular.module('dashboard.controllers', [])

  .controller('MenuController', ['$scope', '$location', function($scope, $location) {
    $scope.menuItems = [{url: '/', name: 'Index'},
                        {url: '/cluster',       name: 'Cluster'},
                        {url: '/clustercost',       name: 'Cost'},
                        {url: '/hdfs',       name: 'HDFS'},
                        {url: '/mrv2job',   name: 'MR Jobs'},
                        {url: '/yarnapps',   name: 'Yarn Apps'},
                        {url: '/sparksqlweb',   name: 'SparkSQL'},
                        {url: '/mvdw',   name: 'MVDW'},
                        {url: '/zeppelin',   name: 'Zeppelin'},
                        {url: '/rstudio',   name: 'RStudio'}
                        ];


    $scope.isActive = function(url)
    {
      return url === $location.path();
    };
  }])

  .controller('IndexController', ['$scope', function($scope) {       
    
  }])

  .controller('HDFSController', ['$scope', function($scope) {

  }])

  .controller('ClusterController', ['$scope', function($scope) {

  }])

  .controller('ClusterCostController', ['$scope', function($scope) {

  }])

  .controller('MRv2JobController', ['$scope', function($scope) {

  }])

  .controller('YarnAppController', ['$scope', function($scope) {

  }])

  .controller('SparkSQLController', ['$scope', function($scope) {

  }])

  .controller('MVDWController', ['$scope', function($scope) {

  }])

  .controller('DrelephantController', ['$scope', function($scope) {

  }])

  .controller('RStudioController', ['$scope', function($scope) {

  }])



