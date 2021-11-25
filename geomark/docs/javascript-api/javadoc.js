function createAccordion(selector) {
  $(selector).each(function() {
    var active = false;
    if (window.location.hash) {
      if ($('a[name="' + window.location.hash.substring(1) + '"]', this).length > 0) {
        active = 0;
      } else if ($(window.location.hash, this).length > 0) {
        active = 0;
      }
    }
    if ($(this).hasClass('open')) {
      $(this).removeClass('open');
      if (active == false) {
        active = 0;
      }
    }
    $(this).accordion({
      icons : {
        header : "ui-icon-triangle-1-e",
        headerSelected : "ui-icon-triangle-1-s"
      },
      collapsible : true,
      active : active,
      autoHeight : true,
      heightStyle: "content"
    });
  });

}

$(document).ready(function() {
  var exampleIndex = 0;
  $('div.htmlExample').each(function() {
    exampleIndex++;
    var text = String($(this).html());
    var id = $(this).attr('id');
    if (!id) {
      id = 'htmlExample' + exampleIndex;
    }
    $(this).removeAttr('id');
    
    $(this).wrap('<div id="' + id +'">');
    var tabDiv = $(this).parent();
    var exampleDiv = $(this).wrap('<div id="' + id +'-example">');
    var ul = $('<ul>');
    $(ul).append('<li><a href="#' + id + '-example">Example</a></li>');
    $(ul).append('<li><a href="#' + id + '-source">View Source</a></li>');
    $(tabDiv).prepend(ul);
    
    var pre = $('<pre class="prettyprint language-html"/>').text(text);
    var source = $('<div id="' + id +'-source"/>');
    source.append(pre);
    $(tabDiv).append(source);
    $(tabDiv).tabs({ heightStyle: "content" });
  });

  $('div.simpleDataTable table').dataTable({
    "bInfo" : false,
    "bJQueryUI" : true,
    "bPaginate" : false,
    "bSort" : false,
    "bFilter" : false,
    "bAutoWidth": false,
    "bRetrieve": true
  });
  
  createAccordion('div.javaMethod');
  createAccordion('div.javaClass');
  createAccordion('div.javaPackage');
  prettyPrint();
  $(':button').button();
  showParents(window.location.hash); 
});
