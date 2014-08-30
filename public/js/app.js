/*global define, angular */

'use strict';

requirejs.config({
    paths: {
        "ace": webjars.path('ace', 'src-min-noconflict/ace')
        ,"ext-language-tools": webjars.path('ace', 'src-min-noconflict/ext-language_tools')
        ,"angular-ui-ace" : webjars.path('angular-ui-ace', 'ui-ace')
        ,"angular-ui-tree": webjars.path('angular-ui-tree', 'angular-ui-tree')
    },
    shim: {
        "angular-ui-ace": [ "angular", "ace"]
        ,"angular-ui-tree": [ "angular"]
        ,"ext-language-tools": ['ace']
    }
});

require(['angular', 'jquery', './controller/main-controller', './directive/main-directive', './filter/main-filter', './service/main-service', './routes/main-routes',
        'angular-ui-router', 'angular-animate',
        'ui-bootstrap-tpls', 'angular-sanitize', 'angular-cookies', 'angular-ui-ace',
        'angular-ui-tree', 'ext-language-tools',
        'angular-file-upload', 'angular-file-upload-shim'],
    function(angular) {

        // Declare app level module which depends on filters, and services
        var module = angular.module('bugattiApp', [
            'ui.router',
            'ui.ace',
            'ui.bootstrap',
            'ui.tree',
            'ngSanitize',
            'ngAnimate',
            'ngCookies',
            'bugattiApp.routes',
            'bugattiApp.filters',
            'bugattiApp.directives',
            'bugattiApp.services',
            'bugattiApp.controllers'
            ]);

        module.run(['$rootScope', '$state', '$stateParams', 'Auth', function($rootScope,   $state,   $stateParams, Auth) {
                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;

                $rootScope.$on("$stateChangeStart", function (event, toState) {
                    Auth.ping(function() {
                        if (toState.data.access === 'anon') {
                            return
                        }
                        if (!Auth.authorize(toState.data.access)) {
                            event.preventDefault();
                            $state.go('home');
                        }
                    }, function() {
                        if (toState.data.access === 'anon') {
                            return
                        }
                        event.preventDefault();
                        $state.go('home');
                        $rootScope.error = "Unauthorized";
                    });
                });

            }]);

        module.config(["$httpProvider", function($httpProvider) {
            var interceptor = ["$rootScope", "$q", "$timeout", function($rootScope, $q, $timeout) {
                return function(promise) {
                    return promise.then(
                        function(response) {
                            return response;
                        },
                        function(response) { // error
                            if (response.status == 400) {
                                alert('参数错误');
                            } else if (response.status == 404) {
                                alert('项目不存在');
                            } else if (response.status == 409) {
                                alert('项目已存在');
                            }
                            return $q.reject(response);
                        }
                    );
                };
            }];
            $httpProvider.responseInterceptors.push(interceptor);
        }]);

        angular.bootstrap(document, ['bugattiApp']);

    });