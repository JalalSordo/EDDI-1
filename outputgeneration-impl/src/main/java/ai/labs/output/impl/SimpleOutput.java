package ai.labs.output.impl;

import ai.labs.expressions.Expression;
import ai.labs.output.IOutputFilter;
import ai.labs.output.IQuickReply;
import ai.labs.output.ISimpleOutput;
import ai.labs.output.model.OutputEntry;

import java.util.*;

/**
 * @author ginccc
 */
public class SimpleOutput implements ISimpleOutput {
    private Map<String, List<OutputEntry>> outputMapper = new HashMap<>();

    @Override
    public void addOutputEntry(OutputEntry outputEntry) {
        String key = outputEntry.getKey();
        if (!outputMapper.containsKey(key)) {
            outputMapper.put(key, new LinkedList<>());
        }

        List<OutputEntry> tmpOutputEntries = outputMapper.get(key);
        if (!tmpOutputEntries.contains(outputEntry)) {
            tmpOutputEntries.add(outputEntry);
            Collections.sort(tmpOutputEntries);
        }
    }

    @Override
    public List<List<OutputEntry>> getOutputs(List<IOutputFilter> outputFilter) {
        List<List<OutputEntry>> outputs = new LinkedList<>();

        for (IOutputFilter filter : outputFilter) {
            List<OutputEntry> entryList = outputMapper.get(filter.getKey());
            if (entryList == null) {
                continue;
            }

            List<OutputEntry> tmpOutputEntries = new LinkedList<>(entryList);

            List<OutputEntry> outputEntries;
            int occurrence = filter.getOccurrence();
            do {
                outputEntries = extractOutputEntryOfSameOccurrence(tmpOutputEntries, occurrence);
                occurrence--;
            } while (outputEntries.isEmpty());

            outputs.add(outputEntries);
        }

        return outputs;
    }

    private List<OutputEntry> extractOutputEntryOfSameOccurrence(List<OutputEntry> outputEntries, int occurrence) {
        int highestOccurrence = -1;
        for (OutputEntry outputEntry : outputEntries) {
            if (highestOccurrence < outputEntry.getOccurrence()) {
                highestOccurrence = outputEntry.getOccurrence();
            }

            if (outputEntry.getOccurrence() == occurrence) {
                highestOccurrence = occurrence;
                break;
            }
        }

        for (int i = 0; i < outputEntries.size(); ) {
            OutputEntry outputEntry = outputEntries.get(i);
            if (outputEntry.getOccurrence() != highestOccurrence) {
                outputEntries.remove(outputEntry);
            } else {
                i++;
            }
        }

        return outputEntries;
    }

    List<String> convertOutputText(List<OutputEntry> outputEntries) {
        List<String> ret = new LinkedList<>();
        for (OutputEntry outputEntry : outputEntries) {
            ret.add(outputEntry.getText());
        }

        return ret;
    }

    List<IQuickReply> convertQuickReplies(List<OutputEntry> possibleOutput) {
        List<IQuickReply> ret = new LinkedList<>();
        for (OutputEntry outputEntry : possibleOutput) {
            for (OutputEntry.QuickReply quickReply : outputEntry.getQuickReplies()) {
                ret.add(new IQuickReply() {
                    @Override
                    public String getValue() {
                        return quickReply.getValue();
                    }

                    @Override
                    public List<Expression> getExpressions() {
                        return quickReply.getExpressions();
                    }
                });
            }
        }

        return ret;
    }
}
