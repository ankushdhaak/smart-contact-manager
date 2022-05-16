console.log("this is java script file")

const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {
		//true
		//off 
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left","0%");
        }else{
		//false
		//shows
		$(".sidebar").css("display" ,  "block");
		$(".content").css("margin-left" , "20%");
		}
		
    };
		
	