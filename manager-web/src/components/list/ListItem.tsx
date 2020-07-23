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
import styles from './ListItem.module.css';
import {Link} from "react-router-dom";
import UseLongPress from "./UseLongPress";

interface ListItemProps<T> {
  link?: { pathname: string, state: any };
  separate?: boolean | { color: string };
  longPressCallback?: () => void;
}

export default class ListItem<T> extends React.Component<ListItemProps<T>, {}> {

  public render() {
    const {link, longPressCallback} = this.props;
    const separatorColor = this.getSeparatorColor();
    return (
      link
        ? <Link to={{pathname: link.pathname, state: link.state}}>
          <div id='listItem' className={`${styles.item}`}
               style={separatorColor ? {borderBottom: `1px solid ${separatorColor}`} : undefined}>
            {this.props.children}
          </div>
        </Link>
        : <UseLongPress callback={longPressCallback && longPressCallback} ms={500}>
          <div id='listItem' className={`${styles.item} white-text`}
               style={separatorColor ? {borderBottom: `1px solid ${separatorColor}`} : undefined}>
            {this.props.children}
          </div>
        </UseLongPress>
    );
  }

  private getSeparatorColor = (): string | undefined => {
    const {separate} = this.props;
    if (typeof separate === 'boolean' && separate) {
      return "black";
    } else if (typeof separate === 'object') {
      return separate.color ? separate.color : "black";
    }
  };
}

