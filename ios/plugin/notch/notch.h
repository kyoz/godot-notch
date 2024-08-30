//
//  notch.h
//  notch
//
//  Created by Kyoz on 30/08/2024.
//

#ifndef HAPTICS_H
#define HAPTICS_H

#ifdef VERSION_4_0
#include "core/object/object.h"
#endif

#ifdef VERSION_3_X
#include "core/object.h"
#endif

class Notch : public Object {

    GDCLASS(Notch, Object);

    static Notch *instance;

public:
    int get_notch_height();
    int get_bottom_safe_inset();

    static Notch *get_singleton();
    
    Notch();
    ~Notch();

protected:
    static void _bind_methods();
};

#endif
