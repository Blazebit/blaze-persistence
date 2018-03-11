var app = angular.module('app', ['ui.grid', 'ui.grid.pagination']);

app.controller('CatCtrl', ['$scope', 'CatService', 'uiGridConstants',
    function ($scope, CatService, uiGridConstants) {
        // initialize table config with default settings
        var tableConfig = {
            pageNumber: 1,
            pageSize: 5,
            // By default, we sort by id since we need at least a single order field to get deterministic results
            sort: [{'field': 'id', 'direction': 'desc'}],
            filter : [],
            previousPageNumber: -1,
            lowest: null,
            highest: null
        };
        // function to load data based on the tableConfig
        var loadData = function () {
            var loader;
            if ($scope.kind === 'cats') {
                loader = CatService.getCats;
            } else {
                loader = CatService.getCatViews;
            }
            loader(
                tableConfig.pageNumber,
                tableConfig.pageSize,
                tableConfig.filter,
                tableConfig.sort,
                tableConfig.previousPageNumber,
                tableConfig.lowest,
                tableConfig.highest
            ).success(function (data) {
                $scope.gridOptions.data = data.content;
                if (data.totalElements !== -1) {
                    $scope.gridOptions.totalItems = data.totalElements;
                }
                if (data.content.length > 0) {
                    tableConfig.lowest = data.content[0];
                    tableConfig.highest = data.content[data.content.length - 1];
                }
            });
        };

        $scope.kind = 'cats';
        $scope.changeKind = function() {
            $scope.gridOptions.columnDefs = [
                {name: 'id', displayName: 'Id'},
                {name: 'name', displayName: 'Name'},
                {name: 'owner.name', displayName: 'Owner name'}
            ];
            if ($scope.kind === 'cats') {
                $scope.gridOptions.columnDefs.push(
                    {name: 'age', displayName: 'Age'
                        , filters: [
                            {
                                condition: uiGridConstants.filter.GREATER_THAN,
                                placeholder: 'greater than'
                            },
                            {
                                condition: uiGridConstants.filter.LESS_THAN,
                                placeholder: 'less than'
                            }]
                    }
                );
            }
            // Manually set the page number and load as apparently, nested change detection doesn't work?
            tableConfig.pageNumber = 1;
            var old = $scope.gridOptions.paginationCurrentPage;
            $scope.gridOptions.paginationCurrentPage = 1;
            // Force reload because of kind change even if we are on the same page
            if (old === 1) {
                loadData();
            }
        };

        // Initial data loading
        loadData();

        $scope.gridOptions = {
            paginationPageSizes: [5, 10, 20],
            paginationPageSize: tableConfig.pageSize,
            enableColumnMenus: false,
            enableFiltering: true,
            useExternalPagination: true,
            useExternalSorting: true,
            useExternalFiltering: true,
            columnDefs: [
                {name: 'id', displayName: 'Id'},
                {name: 'name', displayName: 'Name'},
                {name: 'owner.name', displayName: 'Owner name'},
                {name: 'age', displayName: 'Age'
                    , filters: [
                        {
                            condition: uiGridConstants.filter.GREATER_THAN,
                            placeholder: 'greater than'
                        },
                        {
                            condition: uiGridConstants.filter.LESS_THAN,
                            placeholder: 'less than'
                        }]
                }
            ],
            onRegisterApi: function (gridApi) {
                $scope.gridApi = gridApi;
                gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
                    if (tableConfig.pageSize === pageSize) {
                        tableConfig.previousPageNumber = tableConfig.pageNumber;
                        tableConfig.pageNumber = newPage;
                        loadData();
                    } else {
                        // Don't do keyset pagination when changing the page size and switch to page 1
                        tableConfig.pageSize = pageSize;
                        tableConfig.pageNumber = -1;
                        $scope.gridOptions.paginationCurrentPage = 1;
                    }
                });
                gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
                    tableConfig.previousPageNumber = -1;
                    tableConfig.sort = [];
                    var idSort = 'desc';
                    if (sortColumns.length > 0) {
                        for (var i = 0; i < sortColumns.length; i++) {
                            if (sortColumns[i].field === 'id') {
                                idSort = sortColumns[i].sort.direction;
                            } else {
                                tableConfig.sort.push({'field': sortColumns[i].field, 'direction': sortColumns[i].sort.direction});
                            }
                        }
                    }
                    // Id sorting must always be last to get deterministic results
                    tableConfig.sort.push({'field': 'id', 'direction': idSort});
                    // Skip loading data when we are about to change to page 1 as that is handled by paginationChanged listener
                    if (tableConfig.pageNumber === 1) {
                        loadData();
                    } else {
                        // To avoid sending keyset pagination data, set the current page number to -1 so that the previousPage becomes -1
                        tableConfig.pageNumber = -1;
                        $scope.gridOptions.paginationCurrentPage = 1;
                    }
                });
                gridApi.core.on.filterChanged($scope, function() {
                    var grid = this.grid;
                    tableConfig.filter = [];
                    for (var i = 0; i < grid.columns.length; i++) {
                        var filters = grid.columns[i].filters;
                        var filter = {
                            'field' : grid.columns[i].field,
                            'values' : []
                        };
                        // Extract the filter information into custom filter objects
                        if (filters.length > 1) {
                            if (filters[0].term !== undefined && filters[0].term !== null && filters[0].term !== "") {
                                filter.values.push(filters[0].term);
                                if (filters[1].term !== undefined && filters[1].term !== null && filters[1].term !== "") {
                                    filter.values.push(filters[1].term);
                                    filter.kind = 'BETWEEN';
                                } else {
                                    filter.kind = 'GT';
                                }
                                tableConfig.filter.push(filter);
                            } else {
                                if (filters[1].term !== undefined && filters[1].term !== null && filters[1].term !== "") {
                                    filter.values.push(filters[1].term);
                                    filter.kind = 'LT';
                                    tableConfig.filter.push(filter);
                                }
                            }
                        } else if (filters[0].term !== undefined && filters[0].term !== null && filters[0].term !== "") {
                            if (grid.columns[i].field === 'id') {
                                filter.kind = 'EQ';
                            } else {
                                filter.kind = 'CONTAINS';
                            }
                            filter.values.push(filters[0].term);
                            tableConfig.filter.push(filter);
                        }
                    }
                    // Skip loading data when we are about to change to page 1 as that is handled by paginationChanged listener
                    if (tableConfig.pageNumber === 1) {
                        loadData();
                    } else {
                        // To avoid sending keyset pagination data, set the current page number to -1 so that the previousPage becomes -1
                        tableConfig.pageNumber = -1;
                        $scope.gridOptions.paginationCurrentPage = 1;
                    }
                });
            }
        };
    }]);

app.service('CatService', ['$http', function ($http) {

    function keyset(obj, sort) {
        var tuple = {};
        // A keyset comprises of the values of the field by which we sort
        for (var i = 0; i < sort.length; i++) {
            var fieldParts = sort[i].field.split('.');
            var keysetObj = tuple;
            var objPart = obj;
            // Dereference keyset for the field and create intermediate objects as required
            // Also dereference the object for the field
            for (var j = 0; j < fieldParts.length - 1; j++) {
                if (keysetObj[fieldParts[j]] === undefined) {
                    keysetObj[fieldParts[j]] = {};
                }
                keysetObj = keysetObj[fieldParts[j]];
                objPart = objPart[fieldParts[j]];
            }
            // Finally set the value of the last object's part on the keyset
            keysetObj[fieldParts[fieldParts.length - 1]] = objPart[fieldParts[fieldParts.length - 1]];
        }
        // For determinism, we always also sort by id
        tuple['id'] = obj['id'];
        return encodeURI(JSON.stringify(tuple));
    }

    function getCats(pageNumber, size, filter, sort, previousPageNumber, lowest, highest) {
        return loadCats('rest/cats', pageNumber, size, filter, sort, previousPageNumber, lowest, highest);
    }

    function getCatViews(pageNumber, size, filter, sort, previousPageNumber, lowest, highest) {
        return loadCats('rest/cat-views', pageNumber, size, filter, sort, previousPageNumber, lowest, highest);
    }

    function loadCats(baseUrl, pageNumber, size, filter, sort, previousPageNumber, lowest, highest) {
        pageNumber = pageNumber > 0 ? pageNumber - 1 : 0;
        baseUrl += '?page=' + pageNumber + '&size=' + size;
        for (var i = 0; i < sort.length; i++) {
            baseUrl += '&sort=' + sort[i].field + ',' + sort[i].direction;
        }
        for (var i = 0; i < filter.length; i++) {
            baseUrl += '&filter=' + encodeURI(JSON.stringify(filter[i]));
        }
        if (previousPageNumber !== -1) {
            previousPageNumber = previousPageNumber > 0 ? previousPageNumber - 1 : 0;
            baseUrl += '&prevPage=' + previousPageNumber + '&lowest=' + keyset(lowest, sort) + '&highest=' + keyset(highest, sort)
        }
        return $http({
            method: 'GET',
            url: baseUrl
        });
    }

    return {
        getCats: getCats,
        getCatViews: getCatViews
    };
}]);