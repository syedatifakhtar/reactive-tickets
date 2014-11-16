'use strict';

/**
 * @ngdoc function
 * @name clientApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the clientApp
 */
angular.module('clientApp')
  .controller('MainCtrl', function ($scope,$http) {
    $scope.movies = [];
	$scope.regions = [
    'NCR',
    'BANG',
    'CHEN'
	];
	$scope.selectedRegion='NCR';
	

  $scope.status = {
    isopen: false
  };

  $scope.setRegion = function(region) {
	$scope.movies=[];
	$scope.selectedRegion=region;
	$scope.getMoviesForRegion();
  };
  
  $scope.toggled = function(open) {
    console.log('Dropdown is now: ', open);
  };

  $scope.toggleDropdown = function($event) {
    $event.preventDefault();
    $event.stopPropagation();
    $scope.status.isopen = !$scope.status.isopen;
  };
  
  $scope.getMoviesForRegion = function() {
		$http.get("http://localhost:8080/moviesByArea?region="+$scope.selectedRegion)
		.success(function(data)
		{
			console.log(data);
			$scope.movies=data;
		});
	}
	$scope.getMoviesForRegion();
})


