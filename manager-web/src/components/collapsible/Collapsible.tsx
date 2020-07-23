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

import React, {createRef} from "react";
import M from "materialize-css";
import ResizeObserver from 'resize-observer-polyfill';

interface Props {
  id: string;
  title: string;
  active?: boolean;
  headerClassname?: string;
  bodyClassname?: string;
  onChange?: () => void;
}

interface State {
  isOpen: boolean;
}

export default class extends React.Component<Props, State> {

  state: State = {
    isOpen: this.props.active || false
  };

  private collapsible = createRef<HTMLUListElement>();
  private resizeObserver: (ResizeObserver | null) = null;

  public componentDidMount(): void {
    this.initCollapsible();
    if (this.props.onChange) {
      this.resizeObserver = new ResizeObserver(() => {
        this.props.onChange?.();
      });
      this.resizeObserver.observe(this.collapsible.current as Element);
    }
  }

  componentWillUnmount() {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
    }
  }

  public render() {
    const {id, title, active, headerClassname, bodyClassname, children} = this.props;
    const {isOpen} = this.state;
    return (
      <ul id={id} className="collapsible" ref={this.collapsible} onClick={this.onChange}>
        <li className={active ? "active" : undefined}>
          <ul className={`collapsible-header no-select ${headerClassname}`}>
            <div className={`subtitle`}>{title}</div>
            <i className="material-icons">{isOpen ? 'arrow_drop_down' : 'arrow_drop_up'}</i>
          </ul>
          <div className={`collapsible-body ${bodyClassname}`}>
            {children}
          </div>
        </li>
      </ul>
    );
  }

  private initCollapsible = () => {
    M.Collapsible.init(this.collapsible.current as Element);
  };

  private onChange = () => {
    this.setState(state => ({isOpen: !state.isOpen}));
    this.props.onChange?.();
  }

}