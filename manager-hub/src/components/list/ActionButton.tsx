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

interface Props {
    icon: string;
    tooltip: {
        text?: string,
        deactivatedText?: string,
        activatedText?: string,
        position: 'left' | 'right' | 'bottom' | 'top'
    };
    clickCallback: () => void;
    active?: boolean;
    automatic?: boolean;
    offset?: number;
}

interface State {
    isActive: boolean;
}

export default class ActionButton extends React.Component<Props, State> {

    state: State = {
        isActive: (this.props.automatic && this.props.active) || false
    };

    handleOnClick = () => {
        if (this.props.automatic) {
            this.setState(state => ({isActive: !state.isActive}));
        }
        this.props.clickCallback();
    };

    public render() {
        const {icon, offset, tooltip, automatic} = this.props;
        const {isActive} = this.state;
        return (
            <div className={`fixed-action-btn tooltipped waves-effect btn-floating grey darken-${isActive ? '4' : '3'}`}
                 style={offset ? {right: `${offset * 55 + 23}px`} : undefined}
                 data-position={tooltip.position}
                 data-tooltip={`${automatic ? (isActive ? tooltip.activatedText : tooltip.deactivatedText) : tooltip.text}`}
                 onClick={this.handleOnClick}>
                <i className="large material-icons">{icon}</i>
            </div>
        );
    }

}