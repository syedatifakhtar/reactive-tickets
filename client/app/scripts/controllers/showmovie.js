'use strict';

/**
 * @ngdoc function
 * @name clientApp.controller:ShowMovieDetailCtrl
 * @description
 * # ShowMovieDetailCtrl
 * Controller of the clientApp
 */
angular.module('clientApp')
  .controller('ShowMovieDetailCtrl', function ($scope,$http,$routeParams) {
  
	$scope.movie;
	
	$scope.tweets=[];
    $scope.getMovieDetails = function() {
		$http.get("http://localhost:8080/movie/"+$routeParams.id)
		.success(function(data)
		{
			console.log(data);
			$scope.movie=data;
		});
	}

	$scope.getMovieTweets = function() {
		$http.get("http://localhost:8080/tweets/"+$routeParams.id.replace(/ /g,''))
		.success(function(data)
		{
			console.log(data);
			$scope.tweets=data;
		});
	}
	
		$scope.getMovieDetails();
		$scope.getMovieTweets();
	
})


