/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa.events;

import java.util.EventObject;


/**
 * Created by hansolo on 05.01.16.
 */
public class UpdateEvent extends EventObject {
    public enum EventType { RECALC, REDRAW, RESIZE, LED, LCD, VISIBILITY, INTERACTIVITY, FINISHED, SECTION, ALERT, VALUE };
    public final EventType eventType;

    public UpdateEvent(final Object SRC, final EventType EVENT_TYPE) {
        super(SRC);
        eventType = EVENT_TYPE;
    }
}
