package saadadb.dataloader.mapping;

public enum RepositoryMode {
	/**
	 * Copy the file into the repository
	 */
	COPY,
	/**
	 * Move the file into the repository
	 */
	MOVE,
	/**
	 * Do not touch th file
	 */
	KEEP;
}
