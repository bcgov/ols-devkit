var geomarkUrlId = 0;

function addGeomarkUrlClick() {
  var form = $('#createGeomarkFromGeomarks');
  var urlField = $('input[name="geomarkUrlEntry"]', form);
  var value = urlField.val();
  if (value && form.validate().element(urlField)) {
    addGeomarkUrl(value);
    urlField.val('');
  }
  form.validate().form();
  return false;
}

function addGeomarkUrl(value) {
  var form = $('#createGeomarkFromGeomarks');
  var countField = $('input[name="geomarkUrlCount"]', form);
  var count = countField.val();
  if (count) {
    count++;
  } else {
    count = 1;
  }
  countField.val(count);
  var idNum = ++geomarkUrlId;
  var htmlValue = value.escapeHTML();
  $('#geomarkUrls', form)
      .append(
        '<div id="geomarkUrl'
            + idNum
            + '" class="geomarkUrl"><div style="float: right"><a id="geomarkUrlRemove'
            + idNum
            + '"><img src="/pub/geomark/images/delete.png" title="Remove" alt="Remove" /></a></div>'
            + '<div style="margin-right: 30px">' + htmlValue
            + '</div><input name="geomarkUrl" type="hidden" value="'
            + htmlValue + '" /> </div>');
  $('a#geomarkUrlRemove' + idNum).click(function() {
    $('#geomarkUrl' + idNum).remove();
    var count = countField.val();
    if (count) {
      count--;
    }
    if (count) {
      countField.val(count);
    } else {
      countField.val('');
    }
    form.validate().form();
  });
  return form;
}

$.urlParam = function(name) {
  var results = new RegExp('[\\?&]' + name + '=([^&#]*)')
      .exec(window.location.href);
  if (!results) {
    return 0;
  }
  return results[1] || 0;
};

function setRequired(form, name, required) {
  var input = $('input[name="' + name + '"]', form);
  input.attr('required', required);
  if (input.validator) {
    input.validator('validate');
  }
}

$(document).ready(function() {
  $('input[name="geometryType"]').change(function() {
    var form = $(this).closest('form');
    var required = this.value == 'Any';
    setRequired(form, 'bufferMetres', required);
  });

  $('input[name="geomarkUrlEntry"]').validate('add', function(component) {
    return geomarkUrlCount > 0;
  });
});

String.prototype.escapeHTML = function() {
  return (this.replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(
    /</g,
    '&lt;').replace(/["]/g, '&quot;'));
};
