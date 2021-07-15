import java.io.File

const val SEPARATOR = ","

fun main(args: Array<String>) {
    
    if (args.size > 0){
        val data = File(args[0]).readLines()
        if (data.size > 0){
            generate(data)
			println("-----Done-------")
        } else {
			println("Could not read from ${args[0]}")
		}
    } else {
        println("Specify the file name")
    }

    
}

fun generate(strings: List<String>){
    val lang = strings[0].split(SEPARATOR).count() - 1
	val filled = arrayOf("bat_charging", "bat_unplugged", "bat_phone", "quiet_active")
    val alarm = arrayOf("alarm_once", "alarm_everyday", "alarm_mon_fri", "custom_alarm")
    var alarms = arrayListOf("", "", "", "")
	
    println("Languages: $lang")
	
	val dir = File("app/src/main/res")
	if (!dir.exists())
        dir.mkdirs()
		println("Created output folder")
    
    for (x in 1..lang){
        var output = ""
    	output += "<resources>\n"
        var name = ""
    	for (s in strings){
        	var word = s.split(SEPARATOR)
			val text = word[x].replace("\'", "\\'").replace("&", "&amp;")
            if (word[0] == "id"){
                name = if (word[x] == "en"){
                    "values"
                } else {
                    "values-${word[x]}"
                }
            } else {
                output += "\t<string name=\"${word[0]}\">${if (filled.contains(word[0])) padded(text) else text}</string>\n"
				if (alarm.contains(word[0])){
					val i = alarm.indexOf(word[0])
					alarms[i] = text
				}
            }
    	}
		
		output += "\t<string-array name=\"alarm_options\">\n\t\t<item>${alarms[0]}</item>\n\t\t<item>${alarms[1]}</item>\n\t\t<item>${alarms[2]}</item>\n\t\t<item>${alarms[3]}</item>\n\t</string-array>"
		output += "\n\t<string-array name=\"sedentaryInterval\">\n\t\t<item>45 mins</item>\n\t\t<item>1 hour</item>\n\t</string-array>\n"
    	output += "</resources>"
        println("Generated: ${name}/strings.xml")
		if (!dir.exists())
        dir.mkdirs()
		val res = File(dir, name)
		if (!res.exists())
            res.mkdirs()
		val file = File(res, "strings.xml")
		file.createNewFile()
		file.writeText(output)
	}
}


fun padded(w: String): String{
    val input = w.replace("[", "").replace("]", "")
    if (input.length > 25){
        return input.substring(0, 25)
    } else if (input.length == 25){
        return input
    } else {
        val rem = 25 - input.length
        var start = if (rem/2 != 0){
            "_".repeat(rem/2)
        } else {
            ""
        }
        if (rem % 2 != 0){
            start += "_"
        }
        var end = if (rem/2 != 0){
            "_".repeat(rem/2)
        } else {
            ""
        }
        var pad = start + input + end
        if (pad[0] == '_'){
            pad = "[" + pad.substring(1,25)
        }
        if (pad[24] == '_'){
            pad = pad.substring(0,24) + "]"
        }
        return pad
    }
}