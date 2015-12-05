$(function(){

  $("#clear_code").click(
    function(){
      $("#scalacode").children().remove();
    }
  );

  $("#clear_error_message").click(
    function(){
      $("#error_message").children().remove();
    }
  );

  $("#compile").click(
    function(){
      console.log("click!!");

      var file_name = $("#proto_file_name").val() + ".proto";
      var source_content = $("#protocode").val();
      var sendData = {options:[]};

      sendData['files'] = [{
        name : file_name,
        src : source_content
      }];

      if($("#grpc").is(':checked')){
        sendData["options"] = ["grpc"];
      }

      if($("#java_conversions").is(':checked')){
        sendData["options"] = sendData["options"].concat(["java_conversions"]);
      }

      $("#error_message").children().remove();
      $("#protocode").children().remove();

      console.log(sendData);

      jQuery.post(
        '/',
        JSON.stringify(sendData),
        function(data){
          console.log(data);
          console.log(data.files.length);
          if(! data.error){
            var str = "";
            for(var i = 0; i < data.files.length; i++){
              str += data.files[i].src + "\n\n";
            }
            $("#scalacode").text(str);
            $("#scalacode").attr("class", "source_code prettyprint");
            prettyPrint();
          }else{
            $("#scalacode").children().remove();
            $("#error_message").append("<pre style='color: red;'>" + data.message + "</pre>")
          }
        },
        "JSON"
      );
    }
  );
});

$(document).ready(function(){
  $("#proto_file_name").val("routeguide");

  jQuery.get(
    'https://raw.githubusercontent.com/grpc/grpc-java/v0.9.0/examples/src/main/proto/route_guide.proto',
    function(data){
      console.log(data);
      $("#protocode").val(data);
      $('#compile').click();
    }
  );
});
