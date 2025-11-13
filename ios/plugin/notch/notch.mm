//
//  notch.mm (đổi từ .m sang .mm để sử dụng Objective-C++)
//  notch
//
//  Created by Kyoz on 30/08/2024.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

#ifdef VERSION_4_0
#include "core/object/class_db.h"
#else
#include "core/class_db.h"
#endif

#include "notch.h"

// Observer for orientation changes
@interface NotchOrientationObserver : NSObject
@property (nonatomic, assign) Notch *notchInstance;
- (instancetype)initWithNotch:(Notch *)notch;
- (void)startObserving;
- (void)stopObserving;
@end

@implementation NotchOrientationObserver

- (instancetype)initWithNotch:(Notch *)notch {
    self = [super init];
    if (self) {
        _notchInstance = notch;
    }
    return self;
}

- (void)startObserving {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(orientationDidChange:)
                                                 name:UIDeviceOrientationDidChangeNotification
                                               object:nil];
    
    // Enable device orientation notifications
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
    
    NSLog(@"[NotchPlugin] Orientation observer started");
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIDeviceOrientationDidChangeNotification
                                                  object:nil];
    
    [[UIDevice currentDevice] endGeneratingDeviceOrientationNotifications];
    
    NSLog(@"[NotchPlugin] Orientation observer stopped");
}

- (void)orientationDidChange:(NSNotification *)notification {
    if (_notchInstance) {
        UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
        
        // Map device orientation to string
        NSString *orientationString = [self getOrientationString:orientation];
        
        // Only emit signal for valid orientations (not face up/down/unknown)
        if (![orientationString isEqualToString:@"Unknown"]) {
            NSLog(@"[NotchPlugin] Orientation changed to: %@", orientationString);
            
            // Call C++ method
            _notchInstance->on_orientation_changed(String([orientationString UTF8String]));
        }
    }
}

- (NSString *)getOrientationString:(UIDeviceOrientation)orientation {
    switch (orientation) {
        case UIDeviceOrientationPortrait:
            return @"Portrait";
        case UIDeviceOrientationLandscapeLeft:
            // Note: Device left means UI rotates right (standard landscape)
            return @"Landscape";
        case UIDeviceOrientationPortraitUpsideDown:
            return @"Reverse Portrait";
        case UIDeviceOrientationLandscapeRight:
            // Note: Device right means UI rotates left (reverse landscape)
            return @"Reverse Landscape";
        default:
            return @"Unknown";
    }
}

- (void)dealloc {
    [self stopObserving];
}

@end

// Global observer instance
static NotchOrientationObserver *orientationObserver = nil;

Notch *Notch::instance = NULL;

Notch::Notch() {
    instance = this;
    NSLog(@"[NotchPlugin] Initialize notch");
    
    // Create and start orientation observer
    orientationObserver = [[NotchOrientationObserver alloc] initWithNotch:this];
    [orientationObserver startObserving];
}

Notch::~Notch() {
    NSLog(@"[NotchPlugin] Deinitialize notch");
    
    // Stop and cleanup observer
    if (orientationObserver) {
        [orientationObserver stopObserving];
        orientationObserver = nil;
    }
    
    if (instance == this) {
        instance = NULL;
    }
}

Notch *Notch::get_singleton() {
    return instance;
}

void Notch::_bind_methods() {
    ClassDB::bind_method("get_safe_insets", &Notch::get_safe_insets);
    ClassDB::bind_method("get_current_orientation_string", &Notch::get_current_orientation_string);
    
    // Register signal
    ADD_SIGNAL(MethodInfo("screen_orientation_changed", PropertyInfo(Variant::STRING, "orientation")));
}

void Notch::on_orientation_changed(const String &orientation) {
    // Emit the signal to Godot
    emit_signal("screen_orientation_changed", orientation);
}

String Notch::get_current_orientation_string() {
    UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
    
    switch (orientation) {
        case UIDeviceOrientationPortrait:
            return "Portrait";
        case UIDeviceOrientationLandscapeLeft:
            return "Landscape";
        case UIDeviceOrientationPortraitUpsideDown:
            return "Reverse Portrait";
        case UIDeviceOrientationLandscapeRight:
            return "Reverse Landscape";
        default:
            return "Unknown";
    }
}

Dictionary Notch::get_safe_insets() {
    // Primary check for safeAreaInsets availability (iOS 11.0+)
    if (@available(iOS 11.0, *)) {
        
        // --- CHECK FOR SCENE DELEGATE API (iOS 13.0+) ---
        if (@available(iOS 13.0, *)) {
            // Retrieve safeAreaInsets using the Window Scene (Standard for iOS 13.0+)
            for (UIWindowScene *windowScene in UIApplication.sharedApplication.connectedScenes) {
                // Ensure we only check the active foreground scene
                if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                    // Get the first window from the active scene
                    UIWindow *window = windowScene.windows.firstObject;
                    if (window) {
                        // Get the safe area insets
                        UIEdgeInsets safeInsets = window.safeAreaInsets;

                        // Create a dictionary with all insets as integer values
                        Dictionary insetsDict;
                        insetsDict["top"] = (int)safeInsets.top;
                        insetsDict["bottom"] = (int)safeInsets.bottom;
                        insetsDict["left"] = (int)safeInsets.left;
                        insetsDict["right"] = (int)safeInsets.right;

                        return insetsDict;
                    }
                }
            }
        } else {
            // Fallback for iOS 11.0 - 12.x (Pre-Scene Delegate)
            // NOTE: keyWindow is deprecated in iOS 13.0+, but necessary here
            UIWindow *window = UIApplication.sharedApplication.keyWindow;
            if (window) {
                UIEdgeInsets safeInsets = window.safeAreaInsets;
                
                Dictionary insetsDict;
                insetsDict["top"] = (int)safeInsets.top;
                insetsDict["bottom"] = (int)safeInsets.bottom;
                insetsDict["left"] = (int)safeInsets.left;
                insetsDict["right"] = (int)safeInsets.right;
                
                return insetsDict;
            }
        }
        // --- END SCENE DELEGATE CHECK ---
    }
    
    // Return a dictionary with all zeros if below iOS 11.0 or if no window/insets could be found
    Dictionary emptyInsets;
    emptyInsets["top"] = 0;
    emptyInsets["bottom"] = 0;
    emptyInsets["left"] = 0;
    emptyInsets["right"] = 0;

    return emptyInsets;
}
