/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React from "react";
import LocationMap from "../../../components/map/LocationMap";
import Dialog from "../../../components/dialogs/Dialog";
import {IMarker} from "../../../components/map/Marker";

interface Props {
    launchAppCallback: (position: IMarker) => void;
    markers: IMarker[];
}

interface State {
    selectedPosition?: IMarker
}

export default class LaunchAppDialog extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {};
    }

    private onSelectCoordinates = (marker: IMarker): void =>
        this.setState({selectedPosition: marker});

    private onDeselectCoordinates = (marker: IMarker): void =>
        this.setState({selectedPosition: undefined});

    private launchAppConfirm = () => {
        if (!this.state.selectedPosition) {
            M.toast({html: '<div class="red-text">Error</div><div style="margin-left: 3px"> - location not selected</div>'});
        } else {
            this.props.launchAppCallback(this.state.selectedPosition);
        }
    }

    public render() {
        const location = this.state.selectedPosition ? [this.state.selectedPosition, ...this.props.markers] : this.props.markers;
        return <Dialog id={'launch-app-modal'}
                       title={'Selecionar a localização'}
                       fullscreen={true}
                       locked={true}
                       confirmCallback={this.launchAppConfirm}>
            <LocationMap onSelect={this.onSelectCoordinates} onDeselect={this.onDeselectCoordinates}
                         locations={location}
                         hover clickHighlight/>
        </Dialog>;
    }
}