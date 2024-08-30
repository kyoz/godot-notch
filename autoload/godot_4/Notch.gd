extends Node


var notch = null


# Change this to _ready() if you want automatically init
func init():
	if Engine.has_singleton("Notch"):
		notch = Engine.get_singleton("Notch")


func get_safe_insets():
	if not notch:
		not_found_plugin()
		return {
			"top": 0,
			"bottom": 0,
			"left": 0,
			"right": 0
		}	
	return notch.get_safe_insets()


func not_found_plugin():
	print('[Notch] Not found plugin. Please ensure that you checked Notch plugin in the export template')
