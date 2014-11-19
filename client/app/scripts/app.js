'use strict';

/**
 * @ngdoc overview
 * @name clientApp
 * @description
 * # clientApp
 *
 * Main module of the application.
 */
var clientApp = angular
  .module('clientApp', ['ui.bootstrap','ngRoute']);
function movieRouteConfig($routeProvider) {
	$routeProvider.when('/', {
		templateUrl: 'views/main.html',
				controller:'MainCtrl'
		}).
		when('/movie/:id', {
				templateUrl: 'views/showMovieDetail.html',
		controller:'ShowMovieDetailCtrl'
		}).
		otherwise({
		redirectTo: '/'
		});
}
clientApp.config(movieRouteConfig);
