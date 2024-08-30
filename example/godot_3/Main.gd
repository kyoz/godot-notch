extends Node

onready var notch_lbl = $CanvasLayer/Center/VBoxContainer/NotchLbl
onready var bottom_safe_inset_lbl = $CanvasLayer/Center/VBoxContainer/BottomSafeInsetLbl
onready var top_notch = $CanvasLayer/TopNotch


func _ready():
	Notch.init()
	
	notch_lbl.text = "Top Notch: " + str(Notch.get_notch_height())
	bottom_safe_inset_lbl.text = "Bottom Safe Inset: " + str(Notch.get_bottom_safe_inset())
	top_notch.rect_min_size.y = Notch.get_notch_height()
	


