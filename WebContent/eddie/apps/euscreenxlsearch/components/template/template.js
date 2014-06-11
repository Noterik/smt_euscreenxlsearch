var Template = function () {
    Page.apply(this, arguments);

    this.extraFilters.hide(); //Bootstrap seems to override the display: none on mobile devices. Hide it again, by doing this.
};

Template.prototype = Object.create(Page.prototype);
Template.prototype.name = "search-results";
Template.prototype.extraFilters = jQuery(".extra-option");
Template.prototype.showExtraFiltersButton = jQuery("#show-extra-filters");
Template.prototype.hideExtraFiltersButton = jQuery("#hide-extra-filters");
Template.prototype.events = {
    "click #show-extra-filters": function (event) {
        this.extraFilters.show();
        this.showExtraFiltersButton.hide();
        this.hideExtraFiltersButton.show();
    },
    "click #hide-extra-filters": function (event) {
        this.extraFilters.hide();
        this.hideExtraFiltersButton.hide();
        this.showExtraFiltersButton.show();
    }
};