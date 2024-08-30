extends Node


@onready var notch_lbl: Label = $CanvasLayer/Center/VBoxContainer/NotchLbl
@onready var bottom_safe_inset_lbl: Label = $CanvasLayer/Center/VBoxContainer/BottomSafeInsetLbl
@onready var top_notch: ColorRect = $CanvasLayer/TopNotch


func _ready():
	Notch.init()
	
	notch_lbl.text = "Top Notch: " + str(Notch.get_notch_height())
	bottom_safe_inset_lbl.text = "Bottom Safe Inset: " + str(Notch.get_bottom_safe_inset())
	top_notch.custom_minimum_size.y = Notch.get_notch_height()
