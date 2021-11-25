<link href="https://code.jquery.com/ui/1.11.2/themes/cupertino/jquery-ui.css" rel="stylesheet" type="text/css">
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet" type="text/css">
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<link href="https://cdnjs.cloudflare.com/ajax/libs/jasny-bootstrap/3.1.3/css/jasny-bootstrap.min.css" rel="stylesheet" type="text/css">

<script src="https://code.jquery.com/jquery-1.12.1.min.js" type="text/javascript"></script>
<script src="https://code.jquery.com/ui/1.11.4/jquery-ui.min.js" type="text/javascript"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jasny-bootstrap/3.1.3/js/jasny-bootstrap.min.js" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootbox.js/4.4.0/bootbox.min.js" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.15.0/jquery.validate.min.js" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.15.0/additional-methods.min.js" type="text/javascript"></script>
<script type="text/javascript">
var geomarkUrlId = 0;

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
});

String.prototype.escapeHTML = function() {
  return (this.replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(
    /</g,
    '&lt;').replace(/["]/g, '&quot;'));
};
</script>
<style>
body{ padding-top:0px; padding-bottom:0px;width:700px}
</style>
