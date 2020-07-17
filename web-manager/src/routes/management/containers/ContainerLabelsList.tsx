import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import List from "../../../components/list/List";
import React from "react";
import {IContainer} from "./Container";

interface PortsListProps {
  isLoadingContainer: boolean;
  loadContainerError?: string | null;
  container?: IContainer | null;
}

type Props = PortsListProps;

export default class ContainerLabelsList extends BaseComponent<Props, {}> {

  private labels = (): string[] =>
    Object.entries(this.props.container?.labels || []).map(([key, value]) => `${key} = ${value}`);

  private label = (label: string, index: number): JSX.Element =>
    <ListItem key={index} separate={index !== this.labels().length - 1}>
      <div className={`${styles.listItemContent}`}>
        <span>{label}</span>
      </div>
    </ListItem>;

  public render() {
    const LabelsList = List<string>();
    return (
      <LabelsList isLoading={this.props.isLoadingContainer}
                  error={this.props.loadContainerError}
                  emptyMessage={`No labels associated`}
                  list={this.labels()}
                  show={this.label}/>
    );
  }

}