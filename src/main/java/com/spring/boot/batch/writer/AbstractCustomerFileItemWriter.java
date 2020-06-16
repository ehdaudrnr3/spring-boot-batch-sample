package com.spring.boot.batch.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.WriterNotOpenException;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.support.AbstractFileItemWriter;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.batch.support.transaction.TransactionAwareBufferedWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractCustomerFileItemWriter<T> extends AbstractItemStreamItemWriter<T> implements ResourceAwareItemWriterItemStream<T>, InitializingBean {

	public static final boolean DEFAULT_TRANSACTIONAL = true;

	protected static final Log logger = LogFactory.getLog(AbstractFileItemWriter.class);

	public static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");

	// default encoding for writing to output files - set to UTF-8.
	public static final String DEFAULT_CHARSET = "UTF-8";

	private static final String WRITTEN_STATISTICS_NAME = "written";

	private static final String RESTART_DATA_NAME = "current.count";

	private Resource resource;

	protected OutputState state = null;

	private boolean saveState = true;

	private boolean forceSync = false;

	protected boolean shouldDeleteIfExists = true;

	private boolean shouldDeleteIfEmpty = false;

	private String encoding = DEFAULT_CHARSET;

	private FlatFileHeaderCallback headerCallback;

	private FlatFileFooterCallback footerCallback;

	protected String lineSeparator = DEFAULT_LINE_SEPARATOR;

	private boolean transactional = DEFAULT_TRANSACTIONAL;

	protected boolean append = false;

	/**
	 * Flag to indicate that changes should be force-synced to disk on flush.
	 * Defaults to false, which means that even with a local disk changes could be
	 * lost if the OS crashes in between a write and a cache flush. Setting to true
	 * may result in slower performance for usage patterns involving many frequent
	 * writes.
	 * 
	 * @param forceSync the flag value to set
	 */
	public void setForceSync(boolean forceSync) {
		this.forceSync = forceSync;
	}

	/**
	 * Public setter for the line separator. Defaults to the System property
	 * line.separator.
	 * 
	 * @param lineSeparator the line separator to set
	 */
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	/**
	 * Setter for resource. Represents a file that can be written.
	 * 
	 * @param resource the resource to be written to
	 */
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * Sets encoding for output template.
	 *
	 * @param newEncoding {@link String} containing the encoding to be used for the
	 *                    writer.
	 */
	public void setEncoding(String newEncoding) {
		this.encoding = newEncoding;
	}

	/**
	 * Flag to indicate that the target file should be deleted if it already exists,
	 * otherwise it will be created. Defaults to true, so no appending except on
	 * restart. If set to false and {@link #setAppendAllowed(boolean) appendAllowed}
	 * is also false then there will be an exception when the stream is opened to
	 * prevent existing data being potentially corrupted.
	 * 
	 * @param shouldDeleteIfExists the flag value to set
	 */
	public void setShouldDeleteIfExists(boolean shouldDeleteIfExists) {
		this.shouldDeleteIfExists = shouldDeleteIfExists;
	}

	/**
	 * Flag to indicate that the target file should be appended if it already
	 * exists. If this flag is set then the flag
	 * {@link #setShouldDeleteIfExists(boolean) shouldDeleteIfExists} is
	 * automatically set to false, so that flag should not be set explicitly.
	 * Defaults value is false.
	 * 
	 * @param append the flag value to set
	 */
	public void setAppendAllowed(boolean append) {
		this.append = append;
	}

	/**
	 * Flag to indicate that the target file should be deleted if no lines have been
	 * written (other than header and footer) on close. Defaults to false.
	 * 
	 * @param shouldDeleteIfEmpty the flag value to set
	 */
	public void setShouldDeleteIfEmpty(boolean shouldDeleteIfEmpty) {
		this.shouldDeleteIfEmpty = shouldDeleteIfEmpty;
	}

	/**
	 * Set the flag indicating whether or not state should be saved in the provided
	 * {@link ExecutionContext} during the {@link ItemStream} call to update.
	 * Setting this to false means that it will always start at the beginning on a
	 * restart.
	 * 
	 * @param saveState if true, state will be persisted
	 */
	public void setSaveState(boolean saveState) {
		this.saveState = saveState;
	}

	/**
	 * headerCallback will be called before writing the first item to file. Newline
	 * will be automatically appended after the header is written.
	 *
	 * @param headerCallback {@link FlatFileHeaderCallback} to generate the header
	 *
	 */
	public void setHeaderCallback(FlatFileHeaderCallback headerCallback) {
		this.headerCallback = headerCallback;
	}

	/**
	 * footerCallback will be called after writing the last item to file, but before
	 * the file is closed.
	 *
	 * @param footerCallback {@link FlatFileFooterCallback} to generate the footer
	 *
	 */
	public void setFooterCallback(FlatFileFooterCallback footerCallback) {
		this.footerCallback = footerCallback;
	}

	/**
	 * Flag to indicate that writing to the buffer should be delayed if a
	 * transaction is active. Defaults to true.
	 *
	 * @param transactional true if writing to buffer should be delayed.
	 *
	 */
	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}

	/**
	 * Writes out a string followed by a "new line", where the format of the new
	 * line separator is determined by the underlying operating system.
	 * 
	 * @param items list of items to be written to output stream
	 * @throws Exception if an error occurs while writing items to the output stream
	 */
	@Override
	public void write(List<? extends T> items) throws Exception {
		if (!getOutputState().isInitialized()) {
			throw new WriterNotOpenException("Writer must be open before it can be written to");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Writing to file with " + items.size() + " items.");
		}

		OutputState state = getOutputState();

		String lines = doWrite(items);
		try {
			state.write(lines);
		} catch (IOException e) {
			throw new WriteFailedException("Could not write data. The file may be corrupt.", e);
		}
		state.setLinesWritten(state.getLinesWritten() + items.size());
	}

	/**
	 * Write out a string of items followed by a "new line", where the format of the
	 * new line separator is determined by the underlying operating system.
	 * 
	 * @param items to be written
	 * @return written lines
	 */
	protected abstract String doWrite(List<? extends T> items);

	/**
	 * @see ItemStream#close()
	 */
	@Override
	public void close() {
		super.close();
		if (state != null) {
			try {
				if (footerCallback != null && state.outputBufferedWriter != null) {
					footerCallback.writeFooter(state.outputBufferedWriter);
					state.outputBufferedWriter.flush();
				}
			} catch (IOException e) {
				throw new ItemStreamException("Failed to write footer before closing", e);
			} finally {
				state.close();
				if (state.linesWritten == 0 && shouldDeleteIfEmpty) {
					try {
						resource.getFile().delete();
					} catch (IOException e) {
						throw new ItemStreamException("Failed to delete empty file on close", e);
					}
				}
				state = null;
			}
		}
	}

	/**
	 * Initialize the reader. This method may be called multiple times before close
	 * is called.
	 * 
	 * @see ItemStream#open(ExecutionContext)
	 */
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		super.open(executionContext);

		Assert.notNull(resource, "The resource must be set");
		
		try {
			boolean exists = resource.getFile().exists();
			if (!getOutputState().isInitialized()) {
				try {
					doOpen(executionContext, exists);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void doOpen(ExecutionContext executionContext, boolean exists) throws ItemStreamException, IOException {
		OutputState outputState = getOutputState();
		if (executionContext.containsKey(getExecutionContextKey(RESTART_DATA_NAME))) {
			outputState.restoreFrom(executionContext);
		}
		try {
			outputState.initializeBufferedWriter();
		} catch (IOException ioe) {
			throw new ItemStreamException("Failed to initialize writer", ioe);
		}
		if (outputState.lastMarkedByteOffsetPosition == 0 && !outputState.appending) {
			if (headerCallback != null && !exists) {
				try {
					headerCallback.writeHeader(outputState.outputBufferedWriter);
					outputState.write(lineSeparator);
				} catch (IOException e) {
					throw new ItemStreamException("Could not write headers.  The file may be corrupt.", e);
				}
			}
		}
	}

	/**
	 * @see ItemStream#update(ExecutionContext)
	 */
	@Override
	public void update(ExecutionContext executionContext) {
		super.update(executionContext);
		if (state == null) {
			throw new ItemStreamException("ItemStream not open or already closed.");
		}

		Assert.notNull(executionContext, "ExecutionContext must not be null");

		if (saveState) {

			try {
				executionContext.putLong(getExecutionContextKey(RESTART_DATA_NAME), state.position());
			} catch (IOException e) {
				throw new ItemStreamException("ItemStream does not return current position properly", e);
			}

			executionContext.putLong(getExecutionContextKey(WRITTEN_STATISTICS_NAME), state.linesWritten);
		}
	}

// Returns object representing state.
	protected OutputState getOutputState() {
		if (state == null) {
			File file;
			try {
				file = resource.getFile();
			} catch (IOException e) {
				throw new ItemStreamException("Could not convert resource to file: [" + resource + "]", e);
			}
			Assert.state(!file.exists() || file.canWrite(), "Resource is not writable: [" + resource + "]");
			state = new OutputState();
			state.setDeleteIfExists(shouldDeleteIfExists);
			state.setAppendAllowed(append);
			state.setEncoding(encoding);
		}
		return state;
	}

	/**
	 * Encapsulates the runtime state of the writer. All state changing operations
	 * on the writer go through this class.
	 */
	protected class OutputState {

		private FileOutputStream os;

// The bufferedWriter over the file channel that is actually written
		Writer outputBufferedWriter;

		FileChannel fileChannel;

// this represents the charset encoding (if any is needed) for the
// output file
		String encoding = DEFAULT_CHARSET;

		boolean restarted = false;

		long lastMarkedByteOffsetPosition = 0;

		long linesWritten = 0;

		boolean shouldDeleteIfExists = true;

		boolean initialized = false;

		private boolean append = false;

		private boolean appending = false;

		/**
		 * Return the byte offset position of the cursor in the output file as a long
		 * integer.
		 * 
		 * @return the byte offset position of the cursor in the output file
		 * @throws IOException If unable to get the offset position
		 */
		public long position() throws IOException {
			long pos = 0;

			if (fileChannel == null) {
				return 0;
			}

			outputBufferedWriter.flush();
			pos = fileChannel.position();
			if (transactional) {
				pos += ((TransactionAwareBufferedWriter) outputBufferedWriter).getBufferSize();
			}

			return pos;

		}

		/**
		 * @param append if true, append to previously created file
		 */
		public void setAppendAllowed(boolean append) {
			this.append = append;
		}

		/**
		 * @param executionContext state from which to restore writing from
		 */
		public void restoreFrom(ExecutionContext executionContext) {
			lastMarkedByteOffsetPosition = executionContext.getLong(getExecutionContextKey(RESTART_DATA_NAME));
			linesWritten = executionContext.getLong(getExecutionContextKey(WRITTEN_STATISTICS_NAME));
			if (shouldDeleteIfEmpty && linesWritten == 0) {
				// previous execution deleted the output file because no items were written
				restarted = false;
				lastMarkedByteOffsetPosition = 0;
			} else {
				restarted = true;
			}
		}

		/**
		 * @param shouldDeleteIfExists indicator
		 */
		public void setDeleteIfExists(boolean shouldDeleteIfExists) {
			this.shouldDeleteIfExists = shouldDeleteIfExists;
		}

		/**
		 * @param encoding file encoding
		 */
		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		public long getLinesWritten() {
			return linesWritten;
		}

		public void setLinesWritten(long linesWritten) {
			this.linesWritten = linesWritten;
		}

		/**
		 * Close the open resource and reset counters.
		 */
		public void close() {

			initialized = false;
			restarted = false;
			try {
				if (outputBufferedWriter != null) {
					outputBufferedWriter.close();
				}
			} catch (IOException ioe) {
				throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
			} finally {
				if (!transactional) {
					closeStream();
				}
			}
		}

		private void closeStream() {
			try {
				if (fileChannel != null) {
					fileChannel.close();
				}
			} catch (IOException ioe) {
				throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException ioe) {
					throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
				}
			}
		}

		/**
		 * @param line String to be written to the file
		 * @throws IOException If unable to write the String to the file
		 */
		public void write(String line) throws IOException {
			if (!initialized) {
				initializeBufferedWriter();
			}

			outputBufferedWriter.write(line);
			outputBufferedWriter.flush();
		}

		/**
		 * Truncate the output at the last known good point.
		 * 
		 * @throws IOException if unable to work with file
		 */
		public void truncate() throws IOException {
			fileChannel.truncate(lastMarkedByteOffsetPosition);
			fileChannel.position(lastMarkedByteOffsetPosition);
		}

		/**
		 * Creates the buffered writer for the output file channel based on
		 * configuration information.
		 * 
		 * @throws IOException if unable to initialize buffer
		 */
		private void initializeBufferedWriter() throws IOException {

			File file = resource.getFile();
			FileUtils.setUpOutputFile(file, restarted, append, shouldDeleteIfExists);

			os = new FileOutputStream(file.getAbsolutePath(), true);
			fileChannel = os.getChannel();

			outputBufferedWriter = getBufferedWriter(fileChannel, encoding);
			outputBufferedWriter.flush();

			if (append) {
				// Bug in IO library? This doesn't work...
				// lastMarkedByteOffsetPosition = fileChannel.position();
				if (file.length() > 0) {
					appending = true;
					// Don't write the headers again
				}
			}

			Assert.state(outputBufferedWriter != null, "Unable to initialize buffered writer");
			// in case of restarting reset position to last committed point
			if (restarted) {
				checkFileSize();
				truncate();
			}

			initialized = true;
		}

		public boolean isInitialized() {
			return initialized;
		}

		/**
		 * Returns the buffered writer opened to the beginning of the file specified by
		 * the absolute path name contained in absoluteFileName.
		 */
		private Writer getBufferedWriter(FileChannel fileChannel, String encoding) {
			try {
				final FileChannel channel = fileChannel;
				if (transactional) {
					TransactionAwareBufferedWriter writer = new TransactionAwareBufferedWriter(channel,
							() -> closeStream());

					writer.setEncoding(encoding);
					writer.setForceSync(forceSync);
					return writer;
				} else {
					Writer writer = new BufferedWriter(Channels.newWriter(fileChannel, encoding)) {
						@Override
						public void flush() throws IOException {
							super.flush();
							if (forceSync) {
								channel.force(false);
							}
						}
					};

					return writer;
				}
			} catch (UnsupportedCharsetException ucse) {
				throw new ItemStreamException("Bad encoding configuration for output file " + fileChannel, ucse);
			}
		}

		/**
		 * Checks (on setState) to make sure that the current output file's size is not
		 * smaller than the last saved commit point. If it is, then the file has been
		 * damaged in some way and whole task must be started over again from the
		 * beginning.
		 * 
		 * @throws IOException if there is an IO problem
		 */
		private void checkFileSize() throws IOException {
			long size = -1;

			outputBufferedWriter.flush();
			size = fileChannel.size();

			if (size < lastMarkedByteOffsetPosition) {
				throw new ItemStreamException("Current file size is smaller than size at last commit");
			}
		}

	}

}
