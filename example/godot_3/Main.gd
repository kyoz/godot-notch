extends Node

onready var notch_lbl = $CanvasLayer/Center/VBoxContainer/NotchLbl
onready var bottom_safe_inset_lbl = $CanvasLayer/Center/VBoxContainer/BottomSafeInsetLbl
onready var top_notch = $CanvasLayer/TopNotch


func _ready():
	Notch.init()
	
	var top = Notch.get_safe_insets().top
	var bottom = Notch.get_safe_insets().bottom

	notch_lbl.text = "Top Notch: " + str(top)
	bottom_safe_inset_lbl.text = "Bottom Safe Inset: " + str(bottom)
	top_notch.rect_min_size.y = top
	


