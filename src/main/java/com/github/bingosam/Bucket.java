package com.github.bingosam;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title: Module Information  </p>
 * <p>Description: Function Description  </p>
 * <p>Copyright: Copyright (c) 2020     </p>
 * <p>Create Time: 2020/8/4          </p>
 *
 * @author zhang kunbin
 */
@Data
public class Bucket {

    private String id;

    private Set<String> otherIds;

    private List<Stack> stacks;

    private Map<String, Object> extra;
}
