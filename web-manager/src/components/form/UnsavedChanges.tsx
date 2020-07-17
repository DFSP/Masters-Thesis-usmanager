import React from "react";
import styles from "./UnsavedChanges.module.css";

const UnsavedChanges: React.FC = () =>
  <div className={`${styles.container}`}>
    <div className={`${styles.message}`}>Unsaved changes. Make sure to save them</div>
  </div>;

export default UnsavedChanges;