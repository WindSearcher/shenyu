/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.admin.service.impl;

import com.google.common.collect.Lists;
import org.apache.shenyu.admin.aspect.annotation.Pageable;
import org.apache.shenyu.admin.mapper.DiscoveryMapper;
import org.apache.shenyu.admin.mapper.DiscoveryRelMapper;
import org.apache.shenyu.admin.mapper.DiscoveryUpstreamMapper;
import org.apache.shenyu.admin.mapper.ProxySelectorMapper;
import org.apache.shenyu.admin.mapper.DiscoveryHandlerMapper;
import org.apache.shenyu.admin.model.dto.DiscoveryDTO;
import org.apache.shenyu.admin.model.dto.DiscoveryUpstreamDTO;
import org.apache.shenyu.admin.model.dto.ProxySelectorAddDTO;
import org.apache.shenyu.admin.model.entity.DiscoveryDO;
import org.apache.shenyu.admin.model.entity.DiscoveryHandlerDO;
import org.apache.shenyu.admin.model.entity.DiscoveryRelDO;
import org.apache.shenyu.admin.model.entity.ProxySelectorDO;
import org.apache.shenyu.admin.model.entity.DiscoveryUpstreamDO;
import org.apache.shenyu.admin.model.page.CommonPager;
import org.apache.shenyu.admin.model.page.PageResultUtils;
import org.apache.shenyu.admin.model.query.ProxySelectorQuery;
import org.apache.shenyu.admin.model.vo.ProxySelectorVO;
import org.apache.shenyu.admin.service.ProxySelectorService;
import org.apache.shenyu.admin.utils.ShenyuResultMessage;
import org.apache.shenyu.common.utils.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the {@link org.apache.shenyu.admin.service.ProxySelectorService}.
 */
@Service
public class ProxySelectorServiceImpl implements ProxySelectorService {

    private static final Logger LOG = LoggerFactory.getLogger(ProxySelectorServiceImpl.class);

    private final ProxySelectorMapper proxySelectorMapper;

    private final DiscoveryMapper discoveryMapper;

    private final DiscoveryRelMapper discoveryRelMapper;

    private final DiscoveryUpstreamMapper discoveryUpstreamMapper;

    private final DiscoveryHandlerMapper discoveryHandlerMapper;

    public ProxySelectorServiceImpl(final ProxySelectorMapper proxySelectorMapper, final DiscoveryMapper discoveryMapper,
                                    final DiscoveryUpstreamMapper discoveryUpstreamMapper, final DiscoveryHandlerMapper discoveryHandlerMapper,
                                    final DiscoveryRelMapper discoveryRelMapper) {

        this.proxySelectorMapper = proxySelectorMapper;
        this.discoveryMapper = discoveryMapper;
        this.discoveryRelMapper = discoveryRelMapper;
        this.discoveryUpstreamMapper = discoveryUpstreamMapper;
        this.discoveryHandlerMapper = discoveryHandlerMapper;
    }

    /**
     * listByPage.
     *
     * @param query query
     * @return page
     */
    @Override
    @Pageable
    public CommonPager<ProxySelectorVO> listByPage(final ProxySelectorQuery query) {
        List<ProxySelectorVO> result = Lists.newArrayList();
        List<ProxySelectorDO> proxySelectorDOList = proxySelectorMapper.selectByQuery(query);
        proxySelectorDOList.forEach(proxySelectorDO -> {
            ProxySelectorVO vo = new ProxySelectorVO();
            vo.setId(proxySelectorDO.getId());
            vo.setName(proxySelectorDO.getName());
            vo.setType(proxySelectorDO.getType());
            vo.setForwardPort(proxySelectorDO.getForwardPort());
            vo.setCreateTime(proxySelectorDO.getDateCreated());
            vo.setUpdateTime(proxySelectorDO.getDateUpdated());
            vo.setProps(proxySelectorDO.getProps());
            DiscoveryRelDO discoveryRelDO = discoveryRelMapper.selectByProxySelectorId(proxySelectorDO.getId());
            if (!Objects.isNull(discoveryRelDO)) {
                DiscoveryHandlerDO discoveryHandlerDO = discoveryHandlerMapper.selectById(discoveryRelDO.getDiscoveryHandlerId());
                if (!Objects.isNull(discoveryHandlerDO)) {
                    vo.setListenerNode(discoveryHandlerDO.getListenerNode());
                    vo.setHandler(discoveryHandlerDO.getHandler());
                    DiscoveryDO discoveryDO = discoveryMapper.selectById(discoveryHandlerDO.getDiscoveryId());
                    DiscoveryDTO discoveryDTO = new DiscoveryDTO();
                    BeanUtils.copyProperties(discoveryDO, discoveryDTO);
                    vo.setDiscovery(discoveryDTO);
                    List<DiscoveryUpstreamDO> discoveryUpstreamDOList = discoveryUpstreamMapper.selectByDiscoveryHandlerId(discoveryRelDO.getDiscoveryHandlerId());
                    List<DiscoveryUpstreamDTO> discoveryUpstreamDTOList = Lists.newArrayList();
                    discoveryUpstreamDOList.forEach(e -> {
                        DiscoveryUpstreamDTO discoveryUpstreamDTO = new DiscoveryUpstreamDTO();
                        BeanUtils.copyProperties(e, discoveryUpstreamDTO);
                        discoveryUpstreamDTOList.add(discoveryUpstreamDTO);
                    });
                    vo.setDiscoveryUpstreams(discoveryUpstreamDTOList);
                }
            }
            result.add(vo);
        });
        return PageResultUtils.result(query.getPageParameter(), () -> result);
    }

    /**
     * createOrUpdate.
     *
     * @param proxySelectorAddDTO proxySelectorAddDTO
     * @return the string
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrUpdate(final ProxySelectorAddDTO proxySelectorAddDTO) {
        if (StringUtils.hasLength(proxySelectorAddDTO.getId())) {
            return update(proxySelectorAddDTO);
        } else {
            return create(proxySelectorAddDTO);
        }
    }

    /**
     * delete.
     *
     * @param ids id list
     * @return the string
     */
    @Override
    public String delete(final List<String> ids) {

        proxySelectorMapper.deleteByIds(ids);
        return ShenyuResultMessage.DELETE_SUCCESS;
    }

    /**
     * add proxy selector.
     *
     * @param proxySelectorAddDTO {@link ProxySelectorAddDTO}
     * @return insert data count
     */
    @Override
    public String create(final ProxySelectorAddDTO proxySelectorAddDTO) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        ProxySelectorDO proxySelectorDO = ProxySelectorDO.buildProxySelectorDO(proxySelectorAddDTO);
        String proxySelectorId = proxySelectorDO.getId();
        if (proxySelectorMapper.insert(proxySelectorDO) > 0) {
            String discoveryId = UUIDUtils.getInstance().generateShortUuid();
            DiscoveryDO discoveryDO = DiscoveryDO.builder()
                    .id(discoveryId)
                    .name(proxySelectorAddDTO.getName())
                    .type(proxySelectorAddDTO.getDiscovery().getDiscoveryType())
                    .serverList(proxySelectorAddDTO.getDiscovery().getServerList())
                    .level("2")
                    .dateCreated(currentTime)
                    .dateUpdated(currentTime)
                    .props(proxySelectorAddDTO.getDiscovery().getProps())
                    .build();
            if (discoveryMapper.insertSelective(discoveryDO) > 0) {
                // insert discovery handler
                String discoveryHandlerId = UUIDUtils.getInstance().generateShortUuid();
                DiscoveryHandlerDO discoveryHandlerDO = DiscoveryHandlerDO.builder()
                        .id(discoveryHandlerId)
                        .discoveryId(discoveryId)
                        .dateCreated(currentTime)
                        .dateUpdated(currentTime)
                        .listenerNode(proxySelectorAddDTO.getListenerNode())
                        .handler(proxySelectorAddDTO.getHandler() == null ? "" : proxySelectorAddDTO.getHandler())
                        .props(proxySelectorAddDTO.getProps())
                        .build();
                discoveryHandlerMapper.insertSelective(discoveryHandlerDO);
                DiscoveryRelDO discoveryRelDO = DiscoveryRelDO.builder()
                        .id(UUIDUtils.getInstance().generateShortUuid())
                        .pluginName(proxySelectorAddDTO.getName())
                        .discoveryHandlerId(discoveryHandlerId)
                        .proxySelectorId(proxySelectorId)
                        .selectorId("")
                        .dateCreated(currentTime)
                        .dateUpdated(currentTime)
                        .build();
                discoveryRelMapper.insertSelective(discoveryRelDO);
                List<DiscoveryUpstreamDO> upstreamDOList = Lists.newArrayList();
                if (!CollectionUtils.isEmpty(proxySelectorAddDTO.getDiscoveryUpstreams())) {
                    proxySelectorAddDTO.getDiscoveryUpstreams().forEach(discoveryUpstream -> {
                        DiscoveryUpstreamDO discoveryUpstreamDO = DiscoveryUpstreamDO.builder()
                                .id(UUIDUtils.getInstance().generateShortUuid())
                                .discoveryHandlerId(discoveryHandlerId)
                                .protocol(discoveryUpstream.getProtocol())
                                .url(discoveryUpstream.getUrl())
                                .status(discoveryUpstream.getStatus())
                                .weight(discoveryUpstream.getWeight())
                                .props(discoveryUpstream.getProps())
                                .dateCreated(currentTime)
                                .dateUpdated(currentTime)
                                .build();
                        upstreamDOList.add(discoveryUpstreamDO);
                    });
                    discoveryUpstreamMapper.saveBatch(upstreamDOList);
                }
            }
        }
        return ShenyuResultMessage.CREATE_SUCCESS;
    }

    /**
     * update.
     *
     * @param proxySelectorAddDTO proxySelectorAddDTO
     * @return the string
     */
    public String update(final ProxySelectorAddDTO proxySelectorAddDTO) {
        // update proxy selector
        ProxySelectorDO proxySelectorDO = ProxySelectorDO.buildProxySelectorDO(proxySelectorAddDTO);
        proxySelectorMapper.update(proxySelectorDO);
        // DiscoveryRelDO
        DiscoveryRelDO discoveryRelDO = discoveryRelMapper.selectByProxySelectorId(proxySelectorDO.getId());
        String discoveryHandlerId = discoveryRelDO.getDiscoveryHandlerId();
        DiscoveryHandlerDO discoveryHandlerDO = discoveryHandlerMapper.selectById(discoveryHandlerId);
        // update discovery handler
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        discoveryHandlerDO.setHandler(proxySelectorAddDTO.getHandler());
        discoveryHandlerDO.setListenerNode(proxySelectorAddDTO.getListenerNode());
        discoveryHandlerDO.setProps(proxySelectorAddDTO.getProps());
        discoveryHandlerDO.setDateUpdated(currentTime);
        discoveryHandlerMapper.updateSelective(discoveryHandlerDO);
        // update discovery
        DiscoveryDO discoveryDO = discoveryMapper.selectById(discoveryHandlerDO.getDiscoveryId());
        ProxySelectorAddDTO.Discovery discovery = proxySelectorAddDTO.getDiscovery();
        discoveryDO.setServerList(discovery.getServerList());
        discoveryDO.setDateUpdated(currentTime);
        discoveryDO.setProps(discovery.getProps());
        discoveryMapper.updateSelective(discoveryDO);
        // update discovery upstream list
        int result = discoveryUpstreamMapper.deleteByDiscoveryHandlerId(discoveryHandlerId);
        LOG.info("delete discovery upstreams, count is: {}", result);
        proxySelectorAddDTO.getDiscoveryUpstreams().forEach(discoveryUpstream -> {
            DiscoveryUpstreamDO discoveryUpstreamDO = DiscoveryUpstreamDO.builder()
                    .id(UUIDUtils.getInstance().generateShortUuid())
                    .discoveryHandlerId(discoveryHandlerId)
                    .protocol(discoveryUpstream.getProtocol())
                    .url(discoveryUpstream.getUrl())
                    .status(discoveryUpstream.getStatus())
                    .weight(discoveryUpstream.getWeight())
                    .props(discoveryUpstream.getProps())
                    .dateCreated(currentTime)
                    .dateUpdated(currentTime)
                    .build();
            discoveryUpstreamMapper.insert(discoveryUpstreamDO);
        });
        LOG.info("insert discovery upstreams, count is: {}", proxySelectorAddDTO.getDiscoveryUpstreams().size());
        return ShenyuResultMessage.UPDATE_SUCCESS;
    }
}