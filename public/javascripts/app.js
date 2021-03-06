var brewingTools = angular.module('dashboard', ['kendo.directives', 'dashboard.controllers'])
	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.
		    when('/', {
		      controller: 'IndexController',
		      templateUrl: '/assets/partial/index.html'
		    }).
            when('/cluster', {
                redirectTo: '/cluster/hdfs'
            }).
            when('/cluster/hdfs', {
                controller: 'ClusterController',
                templateUrl: '/assets/partial/cluster_hdfs.html'
            }).
            when('/cluster/mrv1', {
                controller: 'ClusterController',
                templateUrl: '/assets/partial/cluster_mrv1.html'
            }).
            when('/cluster/yarn', {
                controller: 'ClusterController',
                templateUrl: '/assets/partial/cluster_yarn.html'
            }).
            when('/cluster/sparkonyarn', {
                controller: 'ClusterController',
                templateUrl: '/assets/partial/cluster_sparkonyarn.html'
            }).
            when('/clustercost',{
                redirectTo: '/clustercost/overview'
            }).
            when('/clustercost/overview', {
                controller: 'ClusterCostController',
                templateUrl: '/assets/partial/clustercost_overview.html'
            }).
            when('/clustercost/byproject', {
                controller: 'ClusterCostController',
                templateUrl: '/assets/partial/clustercost_byproject.html'
            }).
            when('/clustercost/bill', {
                controller: 'ClusterCostController',
                templateUrl: '/assets/partial/clustercost_bill.html'
            }).
            when('/hdfs',{
                redirectTo: '/hdfs/rawlogsize'
            }).
            when('/hdfs/rawlogsize', {
                controller: 'HDFSController',
                templateUrl: '/assets/partial/hdfs_rawlogsize.html'
            }).
            when('/hdfs/sessionlogsize', {
              controller: 'HDFSController',
              templateUrl: '/assets/partial/hdfs_sessionlogsize.html'
            }).
            when('/hdfs/dwsize', {
              controller: 'HDFSController',
              templateUrl: '/assets/partial/hdfs_dwsize.html'
            }).
            when('/hdfs/viewer', {
                controller: 'HDFSController',
                templateUrl: '/assets/partial/hdfs_viewer.html'
            }).
            when('/mrv2job', {
                redirectTo: '/mrv2job/overview'
            }).
            when('/mrv2job/overview', {
                controller: 'MRv2JobController',
                templateUrl: '/assets/partial/mrv2job_overview.html'
            }).
            when('/mrv2job/today', {
                controller: 'MRv2JobController',
                templateUrl: '/assets/partial/mrv2job_today.html'
            }).
            when('/mrv2job/last2weeks', {
                controller: 'MRv2JobController',
                templateUrl: '/assets/partial/mrv2job_last2weeks.html'
            }).
            when('/mrv2job/failed', {
                controller: 'MRv2JobController',
                templateUrl: '/assets/partial/mrv2job_failed.html'
            }).
            when('/mrv2job/longrunning', {
                controller: 'MRv2JobController',
                templateUrl: '/assets/partial/mrv2job_longrunning.html'
            }).
            when('/mrv2job/timeline', {
                controller: 'MRv2JobController',
                templateUrl: '/assets/partial/mrv2job_timeline.html'
            }).
            when('/mrv2job/jobinfo', {
              controller: 'MRv1JobController',
              templateUrl: '/assets/partial/mrv2job_jobinfo.html'
            }).
            when('/yarnapps', {
                redirectTo: '/yarnapps/overview'
            }).
            when('/yarnapps/overview', {
                controller: 'YarnAppController',
                templateUrl: '/assets/partial/yarnapps_overview.html'
            }).
            when('/yarnapps/timeline', {
                controller: 'YarnAppController',
                templateUrl: '/assets/partial/yarnapps_timeline.html'
            }).
            when('/sparksqlweb', {
                controller: 'SparkSQLController',
                templateUrl: '/assets/partial/sparksqlweb.html'
            }).
            when('/sparksqlweb/history', {
                redirectTo: '/sparksqlweb/history/overview'
            }).
            when('/sparksqlweb/history/overview', {
                controller: 'SparkSQLController',
                templateUrl: '/assets/partial/sparksqlweb_history_overview.html'
            }).
            when('/sparksqlweb/history/detail', {
                controller: 'SparkSQLController',
                templateUrl: '/assets/partial/sparksqlweb_history_detail.html'
            }).
            when('/mvdw', {
                redirectTo: '/mvdw/overview'
            }).
            when('/mvdw/overview', {
                controller: 'MVDWController',
                templateUrl: '/assets/partial/mvdw_overview.html'
            }).
            when('/mvdw/ods', {
                controller: 'MVDWController',
                templateUrl: '/assets/partial/mvdw_ods.html'
            }).
            when('/mvdw/session', {
                controller: 'MVDWController',
                templateUrl: '/assets/partial/mvdw_session.html'
            }).
            when('/mvdw/db', {
                controller: 'MVDWController',
                templateUrl: '/assets/partial/mvdw_db.html'
            }).
            when('/zeppelin', {
                controller: 'ZeppelinController',
                templateUrl: '/assets/partial/zeppelin_overview.html'
            }).
            when('/rstudio', {
              controller: 'RStudioController',
              templateUrl: '/assets/partial/rstudio_overview.html'
            }).
		    otherwise({
		      redirectTo: '/'
		    });

  }]);
