
package org.luwrain.app.commander;

public interface Operation extends Runnable
{
    enum Result {
	OK,
	INTERRUPTED,
	UNEXPECTED_PROBLEM,
	PROBLEM_CREATING_DIRECTORY,
	PROBLEM_READING_FILE,
	PROBLEM_WRITING_FILE,
	INACCESSIBLE_SOURCE,
	PROBLEM_CREATING_SYMLINK,
	PROBLEM_READING_SYMLINK,
	PROBLEM_DELETING,
	DEST_EXISTS_NOT_REGULAR,
	NOT_CONFIRMED_OVERWRITE,
	DEST_EXISTS_NOT_DIR,
	DEST_EXISTS,
    };

    String getOperationName();
    int getPercents();
    void interrupt();
    boolean isFinished();
    Result getFinishCode();
    String getExtInfo();
    boolean finishingAccepted();
}
