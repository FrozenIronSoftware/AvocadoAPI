package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.data.DefaultConfig;
import com.frozenironsoftware.avocado.data.model.Podcast;
import com.frozenironsoftware.avocado.util.DatabaseUtil;
import com.frozenironsoftware.avocado.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sql2o.Connection;

import java.util.ArrayList;
import java.util.List;

public class PodcastDatabaseUtil {
    /**
     * Get podcasts by ids from database
     * @param ids podcast ids
     * @return podcast list or null on error
     */
    @Nullable
    static List<Podcast> getPodcastFromDatabase(@NotNull List<Long> ids) {
        if (ids.size() < 1 || ids.size() > DefaultConfig.ITEM_LIMIT)
            return new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from %s.podcasts where ");
        String sqlEpisodes = "select count(id) from %s.episodes where podcast_id = :podcast_id;";
        sqlEpisodes = String.format(sqlEpisodes, DatabaseUtil.schema);
        for (int idIndex = 1; idIndex <= ids.size(); idIndex++) {
            sql.append("id = :p").append(idIndex);
            if (idIndex < ids.size()) {
                sql.append(" or ");
            }
            if (idIndex == ids.size()) {
                sql.append(";");
            }
        }
        Connection connection = null;
        try {
            connection = DatabaseUtil.getTransaction();
            List<Podcast> podcasts = connection.createQuery(String.format(sql.toString(), DatabaseUtil.schema))
                    .withParams(ids.toArray())
                    .setColumnMappings(Podcast.getColumnMapings())
                    .executeAndFetch(Podcast.class);
            for (Long id : ids) {
                Integer episodeCount = connection.createQuery(sqlEpisodes)
                        .addParameter("podcast_id", id)
                        .executeScalar(Integer.class);
                for (Podcast podcast : podcasts) {
                    if (podcast.getId() == id) {
                        podcast.setEpisodes(episodeCount);
                    }
                }
            }
            connection.commit();
            DatabaseUtil.releaseConnection(connection);
            // Add missing IDs
            for (long id : ids) {
                boolean contains = false;
                for (Podcast podcast : podcasts) {
                    if (podcast.getId() == id) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    Podcast podcast = new Podcast();
                    podcast.setId(id);
                    podcast.setPlaceholder(true);
                    podcasts.add(podcast);
                }
            }
            return podcasts;
        }
        catch (Exception e) {
            Logger.exception(e);
            DatabaseUtil.releaseConnection(connection);
            return null;
        }
    }
}
