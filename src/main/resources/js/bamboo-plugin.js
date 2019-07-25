/*
 *     Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

(function ($) { // this closure helps us keep our variables to ourselves.
    // This pattern is known as an "iife" - immediately invoked function expression
    var octanePluginContext = {};
    octanePluginContext.octaneAdminBaseUrl = AJS.contextPath() + "/rest/octane-admin/1.0/";
// wait for the DOM (i.e., document "skeleton") to load. This likely isn't necessary for the current case,
// but may be helpful for AJAX that provides secondary content.
    var spaceTable;

    AJS.$(document).ready(function () {
        window.onbeforeunload = null;//Disable “Changes you made may not be saved” pop-up window
        configureSpaceConfigurationTable();
        configureSpaceConfigurationDialog();

    });

    function configureSpaceConfigurationDialog() {
        //show
        AJS.$("#show-add-dialog").on('click', function (e) {
            spaceTable.currentRow = null;
            e.preventDefault();
            AJS.$("#location").val("");
            AJS.$("#clientId").val("");
            AJS.$("#clientSecret").val("");
            AJS.$("#bambooUser").val("");
            AJS.$("#dialog-message").css("visibility","hidden");
            AJS.dialog2("#config-dialog").show();
        })

        //cancel
        AJS.$("#dialog-cancel-button").on('click', function (e) {
            e.preventDefault();
            AJS.dialog2("#config-dialog").hide();
        });

        AJS.$("#dialog-testconnection-button").on('click', function (e) {
            e.preventDefault();
            if (!validateRequiredFieldsFilled()) {
                console.log("invalid");
                return;
            }
            var throbber = AJS.$("#dialog-message");
            var model = {
                id: "",
                location: AJS.$("#location").val(),
                clientId: AJS.$("#clientId").val(),
                clientSecret: AJS.$("#clientSecret").val(),
                bambooUser: AJS.$("#bambooUser").val()
            };
            throbber.css("visibility","visible");
            testConnection(throbber, model);
        });
        //save
        AJS.$("#dialog-submit-button").on('click', function (e) {
            e.preventDefault();
            console.log("submit");
            if (!validateRequiredFieldsFilled()) {
                console.log("invalid");
                return;
            }
            var model = {
                id: "",
                location: $("#location").val(),
                clientId: $("#clientId").val(),
                clientSecret: $("#clientSecret").val(),
                bambooUser: $("#bambooUser").val()
            };

            if (spaceTable.currentRow)//update
            {
                model.id = spaceTable.currentRow.model.attributes.id;
                var myJSON = JSON.stringify(model);
                console.log("json ", myJSON);
                $.ajax({
                    url: spaceTable.options.resources.self,
                    type: "PUT",
                    data: myJSON,
                    dataType: "json",
                    contentType: "application/json"
                }).done(function (msg) {
                    reloadTable(spaceTable);
                }).fail(function (request, status, error) {
                    alert(request.responseText);
                });
            } else {//add
                var myJSON = JSON.stringify(model);
                console.log("json ", myJSON);
                $.ajax({
                    url: spaceTable.options.resources.self,
                    type: "POST",
                    data: myJSON,
                    dataType: "json",
                    contentType: "application/json"
                }).done(function (msg) {
                    reloadTable(spaceTable);
                }).fail(function (request, status, error) {
                    alert(request.responseText);
                });
            }
            AJS.dialog2("#config-dialog").hide();
        });

    }

    function configureSpaceConfigurationTable() {
        var MyRow = AJS.RestfulTable.Row.extend({
            renderOperations: function () {
                var rowInstance = this;

                var editButtonEl = $('<button class=\"aui-button aui-button-link\">Edit</button>').click(function (e) {
                    spaceTable.currentRow = rowInstance;
                    showSpaceConfigurationDialog(rowInstance);
                });

                var testConnectionButtonEl = $('<button class=\"aui-button aui-button-link\">Test Connection</button>').click(function (e) {
                    var statusEl = rowInstance.$el.children().eq(4);
                    var throbber = statusEl.children().first();


                    var model = {
                        id: rowInstance.model.attributes.id,
                        location: rowInstance.model.attributes.location,
                        clientId: rowInstance.model.attributes.clientId,
                        clientSecret: rowInstance.model.attributes.clientSecret,
                        bambooUser: rowInstance.model.attributes.bambooUser
                    };
                    testConnection(throbber, model);
                });

                var deleteButtonEl = $('<button class=\"aui-button aui-button-link\">Delete</button>').click(function (e) {
                    removeSpaceConfiguration(rowInstance);
                });

                var parentEl = $('<div></div>').append(editButtonEl, deleteButtonEl, testConnectionButtonEl);
                return parentEl;
            }
        });
        spaceTable = new AJS.RestfulTable({
            el: jQuery("#configuration-rest-table"),
            resources: {
                all: octanePluginContext.octaneAdminBaseUrl + "space-config/all",
                self: octanePluginContext.octaneAdminBaseUrl + "space-config/self"
            },
            columns: [
                {id: "location", header: "Location"},
                {id: "clientId", header: "Client ID"},
                {id: "bambooUser", header: "Bamboo user"}
            ],
            autoFocus: false,
            allowEdit: false,
            allowReorder: false,
            allowCreate: false,
            allowDelete: false,
            noEntriesMsg: "No space configuration is defined.",
            loadingMsg: "Loading ...",
            views: {
                row: MyRow
            }
        });


    }

    function validateRequiredFieldsFilled() {
        //validate
        var validationFailed = !validateMissingRequiredField($("#location").val(), "#locationError");
        validationFailed = !validateMissingRequiredField($("#clientId").val(), "#clientIdError") || validationFailed;
        validationFailed = !validateMissingRequiredField($("#clientSecret").val(), "#clientSecretError") || validationFailed;
        validationFailed = !validateMissingRequiredField($("#bambooUser").val(), "#bambooUserError") || validationFailed;
        return !validationFailed;
    }

    function validateMissingRequiredField(value, errorSelector) {
        return validateConditionAndUpdateErrorField(value, 'Value is missing', errorSelector);
    }

    function validateConditionAndUpdateErrorField(condition, errorMessage, errorSelector) {
        if (!condition) {
            $(errorSelector).text(errorMessage);
            return false;
        } else {
            $(errorSelector).text('');
            return true;
        }
    }

    function removeSpaceConfiguration(row) {

        $("#space-to-delete").text(row.model.attributes.location);

        AJS.dialog2("#warning-dialog").show();
        AJS.$("#warning-dialog-confirm").click(function (e) {
            e.preventDefault();
            AJS.dialog2("#warning-dialog").hide();

            console.log("delete function ", spaceTable.options.resources.self + "/" + row.model.id);
            $.ajax({
                url: spaceTable.options.resources.self + "/" + row.model.id, type: "DELETE",
            }).done(function () {
                reloadTable(spaceTable);
            }).fail(function (request, status, error) {
                console.log("fail", request, status, error);
                alert(request.responseText);
            });
        });

        AJS.$("#warning-dialog-cancel").click(function (e) {
            e.preventDefault();
            AJS.dialog2("#warning-dialog").hide();
        });
    }

    function showSpaceConfigurationDialog(rowForEdit) {
        var editMode = !!rowForEdit;
        var editEntity = editMode ? rowForEdit.model.attributes : null;


        function onCloseCallback(result) {
            if (result && result.entity) {
                if (editMode) {
                    var rowModel = rowForEdit.model.attributes;
                    rowModel.location = result.entity.location;
                    rowModel.clientId = result.entity.clientId;
                    rowModel.clientSecret = result.entity.clientSecret;
                    rowModel.bambooUser = result.entity.bambooUser;
                    rowForEdit.render();
                    //    reloadTable(spaceTable);
                } else {
                    spaceTable.addRow(result.entity);
                }
            }
        }


        AJS.$("#location").val(editEntity ? editEntity.location : "");
        AJS.$("#clientId").val(editEntity ? editEntity.clientId : "");
        AJS.$("#clientSecret").val(editEntity ? editEntity.clientSecret : "");
        AJS.$("#bambooUser").val(editEntity ? editEntity.bambooUser : "");
        AJS.dialog2("#config-dialog").show();

    }

    function reloadTable(table) {
        console.log("reloadTable");
        table.$tbody.empty();
        table.fetchInitialResources();
    }

    function testConnection(throbber, model) {
        throbber.addClass("test-connection-status");
        throbber.removeClass("test-connection-status-successful");
        throbber.removeClass("test-connection-status-failed");
        throbber.attr("title", "Testing connection ...");

        console.log("test connection model - ", model);
        var myJSON = JSON.stringify(model);
        console.log("json ", myJSON);
        $.ajax({
            url: octanePluginContext.octaneAdminBaseUrl + "test/testconnection",
            type: "POST",
            data: myJSON,
            dataType: "json",
            contentType: "application/json"
        }).done(function () {
            throbber.addClass("test-connection-status-successful");
            throbber.attr("title", "Test connection is successful");
        }).fail(function (request, status, error) {
            throbber.addClass("test-connection-status-failed");
            throbber.attr("title", "Test connection is failed : " +request.responseText);

        });

    }


    AJS.$(document).bind(AJS.RestfulTable.Events.INITIALIZED, function () {
        //update name of action column that is second from end
        //last two columns don't have name : action column and loading indicator used when editing
        //$("#configuration-rest-table th:nth-last-child(2)").each(function () {
        //this.innerHTML = 'Actions';
        //});
    });

}(AJS.$ || jQuery));
