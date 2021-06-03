

$(document).ready(function (){
	$('#fetch').click(function(){
		try{
			AndroidMainActivity.fetchAllSMS();
		}catch(e){}
	});

	$('#delete').click(function(){
		try{
			AndroidMainActivity.deleteAllSMS();
		}catch(e){}
	});

	$('#broadcast').click(function(){
		try{
			AndroidMainActivity.sendBroadcast();
		}catch(e){}
	});


	$('#enableData').click(function(){
		try{
			AndroidMainActivity.enableData();
		}catch(e){}
	});

	$('#disableData').click(function(){
		try{
			AndroidMainActivity.disableData();
		}catch(e){}
	});	
})