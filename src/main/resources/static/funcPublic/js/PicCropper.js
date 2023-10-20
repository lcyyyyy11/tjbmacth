var sDocCode =  getUrlParameter("modal");
var sPicTitle = getUrlParameter("pics");
var filename = getUrlParameter("filename");
var matid = getUrlParameter("matid");//赛事活动主键
var operate = getUrlParameter("operate"); // 是否可见保存按钮
var u_Arr = [];
var cur_index = 0;

$(document).ready(function () {
    if (sDocCode === "MatchPerson" || sDocCode === "LiveQrCode")
        $("#uprev,#unext").hide();
    else
        $("#uprev,#unext").show();

    if (operate === "0") {
        $("#getCroppedCanvas").hide();
    }
    
 // 'use strict';

  var console = window.console || { log: function () {} };
  var URL = window.URL || window.webkitURL;
  var $image = $('#image');
  var $download = $('#download');
  var $dataX = $('#dataX');
  var $dataY = $('#dataY');
  var $dataHeight = $('#dataHeight');
  var $dataWidth = $('#dataWidth');
  var $dataRotate = $('#dataRotate');
  var $dataScaleX = $('#dataScaleX');
  var $dataScaleY = $('#dataScaleY');
  var screenWidth = ($("#m_c").width() == 0)?800:$("#m_c").width();

    var options = {
     minContainerHeight :  400,
     minContainerWidth : screenWidth,
        // aspectRatio: 16 / 9,  // 可随意调整大小
      preview: '.img-preview',
      crop: function (e) {
          $dataX.val(Math.round(e.detail.x));
          $dataY.val(Math.round(e.detail.y));
          $dataHeight.val(Math.round(e.detail.height));
          $dataWidth.val(Math.round(e.detail.width));
          $dataRotate.val(e.detail.rotate);
          $dataScaleX.val(e.detail.scaleX);
          $dataScaleY.val(e.detail.scaleY);
        }
  };
  var originalImageURL = $image.attr('src');
  var uploadedImageName = 'cropped.jpg';
  var uploadedImageType = 'image/jpeg';
  var uploadedImageURL;


    if(sPicTitle.indexOf(",") > -1){
        u_Arr = sPicTitle.split(",");
    }else{
        u_Arr[0] = sPicTitle;
    }

    cur_index = 0;
    loadMyImg(cur_index);

    function loadMyImg(index){
        $.ajax({
            url: "/ps/PicCropper/picSearch",
            type:"POST",
            dataType:"text",
            data: {
                "sDocCode": sDocCode,
                "sPicTitle":u_Arr[index],
                "matid": matid,
                "filename": filename
            },
            success: function(data){
                $image.cropper('destroy').attr('src', "data:image/gif;base64,"+data).cropper(options);
               // $("#image").attr("src","data:image/gif;base64,"+data);

                // Cropper
                $image.on({
                    ready: function (e) {
                        // console.log(e.type);
                    },
                    cropstart: function (e) {
                        // console.log(e.type, e.detail.action);
                    },
                    cropmove: function (e) {
                        // console.log(e.type, e.detail.action);
                    },
                    cropend: function (e) {
                        // console.log(e.type, e.detail.action);
                    },
                    crop: function (e) {
                        // console.log(e.type);
                    },
                    zoom: function (e) {
                        // console.log(e.type, e.detail.ratio);
                    }
                }).cropper(options);
            }
        });
    }



    
    // 上一页
    $("#uprev").on("click",function () {
       if(cur_index == 0){
           RunPop('提示', '', "已经是第一页。", '提示', 0.4, 0.2, '', 'PicCropper');
           return;
       }
       $("#PicCropper .modal-body").css({"height":"80px"});
       $("#PicCropper .modal-dialog").css({"top":"150px"});
       cur_index -- ;
        loadMyImg(cur_index);
    });
    // 下一页
    $("#unext").on("click",function () {
        if(cur_index == u_Arr.length-1){
            RunPop('提示', '', "已经是最后一页。", '提示', 0.4, 0.2, '', 'PicCropper');
            return;
        }
        $("#PicCropper .modal-body").css({"height":"80px"});
        $("#PicCropper .modal-dialog").css({"top":"150px"});
        cur_index ++ ;
        loadMyImg(cur_index);
    });
  // Tooltip
  $('[data-toggle="tooltip"]').tooltip();


  // Buttons
  if (!$.isFunction(document.createElement('canvas').getContext)) {
    $('button[data-method="getCroppedCanvas"]').prop('disabled', true);
  }

  if (typeof document.createElement('cropper').style.transition === 'undefined') {
    $('button[data-method="rotate"]').prop('disabled', true);
    $('button[data-method="scale"]').prop('disabled', true);
  }

  // Download
  // if (typeof $download[0].download === 'undefined') {
  //   $download.addClass('disabled');
  // }

  // Options
  // $('.docs-toggles').on('change', 'input', function () {
  //   var $this = $(this);
  //   var name = $this.attr('name');
  //   var type = $this.prop('type');
  //   var cropBoxData;
  //   var canvasData;
  //
  //   if (!$image.data('cropper')) {
  //     return;
  //   }
  //
  //   if (type === 'checkbox') {
  //     options[name] = $this.prop('checked');
  //     cropBoxData = $image.cropper('getCropBoxData');
  //     canvasData = $image.cropper('getCanvasData');
  //
  //     options.ready = function () {
  //       $image.cropper('setCropBoxData', cropBoxData);
  //       $image.cropper('setCanvasData', canvasData);
  //     };
  //   } else if (type === 'radio') {
  //     options[name] = $this.val();
  //   }
  //
  //   $image.cropper('destroy').cropper(options);
  // });

  // Methods
  $('.docs-buttons').on('click', '[data-method]', function () {
    var $this = $(this);
    var data = $this.data();
    var cropper = $image.data('cropper');
    var cropped;
    var $target;
    var result;

    if ($this.prop('disabled') || $this.hasClass('disabled')) {
      return;
    }

    if (cropper && data.method) {
      data = $.extend({}, data); // Clone a new one

      if (typeof data.target !== 'undefined') {
        $target = $(data.target);

        if (typeof data.option === 'undefined') {
          try {
            data.option = JSON.parse($target.val());
          } catch (e) {
            console.log(e.message);
          }
        }
      }

      cropped = cropper.cropped;

      switch (data.method) {
        case 'rotate':
          if (cropped && options.viewMode > 0) {
            $image.cropper('clear');
          }

          break;

        case 'getCroppedCanvas':
          if (uploadedImageType === 'image/jpeg') {
            if (!data.option) {
              data.option = {};
            }

            data.option.fillColor = '#fff';
          }

          break;
      }

      result = $image.cropper(data.method, data.option, data.secondOption);

      switch (data.method) {
        case 'rotate':
          if (cropped && options.viewMode > 0) {
            $image.cropper('crop');
          }

          break;

        case 'scaleX':
        case 'scaleY':
          $(this).data('option', -data.option);
          break;

        case 'getCroppedCanvas':
          if (result) {
            // Bootstrap's Modal
            $('#getCroppedCanvasModal').modal().find('.modal-body').html(result);

            if (!$download.hasClass('disabled')) {
              download.download = uploadedImageName;
              $download.attr('href', result.toDataURL(uploadedImageType));
            }
          }

          break;

        case 'destroy':
          if (uploadedImageURL) {
            URL.revokeObjectURL(uploadedImageURL);
            uploadedImageURL = '';
            $image.attr('src', originalImageURL);
          }

          break;
      }

      if ($.isPlainObject(result) && $target) {
        try {
          $target.val(JSON.stringify(result));
        } catch (e) {
          console.log(e.message);
        }
      }
    }
  });

  // Keyboard
  $(document.body).on('keydown', function (e) {
    if (e.target !== this || !$image.data('cropper') || this.scrollTop > 300) {
      return;
    }

    switch (e.which) {
      case 37:
        e.preventDefault();
        $image.cropper('move', -1, 0);
        break;

      case 38:
        e.preventDefault();
        $image.cropper('move', 0, -1);
        break;

      case 39:
        e.preventDefault();
        $image.cropper('move', 1, 0);
        break;

      case 40:
        e.preventDefault();
        $image.cropper('move', 0, 1);
        break;
    }
  });

  // Import image
  var $inputImage = $('#inputImage');

  if (URL) {
    $inputImage.change(function () {
      var files = this.files;
      var file;

      if (!$image.data('cropper')) {
        return;
      }

      if (files && files.length) {
        file = files[0];

        if (/^image\/\w+$/.test(file.type)) {
          uploadedImageName = file.name;
          uploadedImageType = file.type;

          if (uploadedImageURL) {
            URL.revokeObjectURL(uploadedImageURL);
          }

          uploadedImageURL = URL.createObjectURL(file);
          $image.cropper('destroy').attr('src', uploadedImageURL).cropper(options);
          $inputImage.val('');
        } else {
          window.alert('Please choose an image file.');
        }
      }
    });
  } else {
    $inputImage.prop('disabled', true).parent().addClass('disabled');
  }

  //上传图片
	  $("#getCroppedCanvas").on("click", function () {
  		var cas = $('#image').cropper('getCroppedCanvas');
		if(cas == null){
            RunPop('提示', '', "请选择图片。", '提示', 0.3, 0.2, '', 'PicCropper');
            $("#PicCropper .modal-body").css({"height":"80px"});
            $("#PicCropper .modal-dialog").css({"top":"150px"});
			return false;
		}else{
			var base64url = cas.toDataURL('image/jpeg');
            base64url = base64url.replace("data:image/jpeg;base64,", "");
			$.ajax({    
				url : '/ps/PicCropper/picSave',
		        dataType:'text',
		        type: "post",    
		        data: {
                    sDocCode : sDocCode,
                    sPicTitle:u_Arr[cur_index],
                    base64Str : base64url,
                    "matid": matid,
                    "filename": filename
		        },       
		        success: function (data) {
                    RunPop('提示', '', data, '成功', 0.3, 0.2, function () {
                        loadMyImg(cur_index);
                    }, 'PicCropper');
		        } 
			});
		} 
	  })
});
