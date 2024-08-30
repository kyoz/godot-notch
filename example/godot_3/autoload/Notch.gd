extends Node


var notch = null


# Change this to _ready() if you want automatically init
func init():
	if Engine.has_singleton("Notch"):
		notch = Engine.get_singleton("Notch")


func get_notch_height():
	if not notch:
		not_found_plugin()
		return 0
	
	return notch.get_notch_height()


func get_bottom_safe_inset():
	if not notch:
		not_found_plugin()
		return 0
	
	return notch.get_bottom_safe_inset()


func not_found_plugin():
	print('[Notch] Not found plugin. Please ensure that you checked Notch plugin in the export template')
