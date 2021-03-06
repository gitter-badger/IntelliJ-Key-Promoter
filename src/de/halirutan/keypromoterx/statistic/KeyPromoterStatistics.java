/*
 * Copyright (c) 2017 Patrick Scheibe, Dmitry Kashin, Athiele.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.halirutan.keypromoterx.statistic;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.*;
import org.jetbrains.annotations.Nullable;
import de.halirutan.keypromoterx.KeyPromoterAction;

import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Provides storing the statistics persistently. Note that the only thing we store persistently is the list of
 * {@link StatisticsItem}. All other functionality that is used e.g. to communicate changes in the statistic to the UI
 * or methods to register new button-presses are marked as @Transient meaning they are ignored by the persistent state
 * framework.
 * </br>
 * The @MapAnnotation defines how the statistics map is laid out as xml file on disk. This is pure cosmetics in our case.
 * </br>
 * Furthermore, since we use {@link StatisticsItem} as underlying data-structure, it is very easy to add further features.
 * One could for instance include the times when buttons are pressed to create a graph that shows if the user really
 * progresses in replacing mouse actions with shortcuts.
 * @author Patrick Scheibe
 */
@State(
        name = "KeyPromoterStatistic",
        storages = {
                @Storage(
                        file = "$APP_CONFIG$/KeyPromoterStatistic.xml",
                        id = "KeyPromoterStatistic"
                )}
)
public class KeyPromoterStatistics implements PersistentStateComponent<KeyPromoterStatistics> {

    @MapAnnotation(surroundKeyWithTag = false, surroundValueWithTag = false, surroundWithTag = false, entryTagName = "Statistic", keyAttributeName = "Action")
    private Map<String , StatisticsItem> statistics = Collections.synchronizedMap(new HashMap<String, StatisticsItem>());

    @Transient
    private PropertyChangeSupport myChangeSupport;

    @Nullable
    @Override
    public KeyPromoterStatistics getState() {
        return this;
    }

    @Override
    public void loadState(KeyPromoterStatistics stats) {
        XmlSerializerUtil.copyBean(stats, this);
    }

    @Transient
    void registerPropertyChangeSupport(PropertyChangeSupport support) {
        myChangeSupport = support;
    }

    @Transient
    public void registerAction(KeyPromoterAction action) {
        statistics.putIfAbsent(action.getDescription(), new StatisticsItem(action));
        statistics.get(action.getDescription()).registerEvent();
        myChangeSupport.firePropertyChange("stats", null, null);
    }

    @Transient
    public void resetStatistic() {
        statistics.clear();
        myChangeSupport.firePropertyChange("stats", null, null);
    }

    @Transient
    public StatisticsItem get(KeyPromoterAction action) {
        return statistics.get(action.getDescription());
    }

    @Transient
    ArrayList<StatisticsItem> getStatisticItems() {
        final ArrayList<StatisticsItem> items = new ArrayList<>(statistics.values());
        Collections.sort(items);
        return items;
    }
}
